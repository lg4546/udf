package com.smindu.util;

import org.apache.hadoop.hive.ql.exec.UDF;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 烟量 统一转换为 多少 支/天
 */
public class GetZhiUntil extends UDF {

//	public static void main(String[] args) {
////		Test1 t = new Test1();
//		String yan_liang_matchertext = "[0-9]{1,7}[支包]{1,}/天"; // 烟量正则表达式
////		String a = t.getMatcherResult(yan_liang_matchertext,"最近2周吸烟5支/天吸了20年"); // 5支/天
//		System.out.println(getMatcherResult(yan_liang_matchertext,replaceStr("最近2周吸烟5支/天吸了20年")));// 5支/天
//		System.out.println(getMatcherResult(yan_liang_matchertext,replaceStr("平时10只/日发病期间2-3包/日"))); // 10支/天
//		System.out.println(getMatcherResult(yan_liang_matchertext,replaceStr("最近2周发病期间2-3包/日"))); // 3包/天
//		System.out.println(getZhi("最近2周发病期间2-3包/日")); // 60.0
//	}
	
	public static String evaluate(String str){
		String result = null; // 声明返回结果变量
		String yan_liang_matchertext = "[0-9]{1,7}[支包]{1,}/天"; // 烟量正则表达式

		// 1 循环 调用 替换方法 根据替换列表中的每个字符串，替换掉为指定字符串。
		str = replaceStr(str);
		
		// 2 用正则 提取 烟量
		str = getMatcherResult(yan_liang_matchertext,str);
		// System.out.println("替换指定内容为空白后的字符串="+str); // 替换指定内容为空白后的字符串=1年

		// 3 定义 递归函数 返回标准 多少 支/天
		result = String.valueOf(transform_zhi(str));

		return result;
	}
	
	
	/**
	 * 替换同义字 为 标准字
	 * @param str
	 * @return
	 */
	public static String replaceStr(String str){
		for(int i=0;i< NumberUtils.need_replace_str_list_before.length;i++){
			// 根据替换列表中的每个字符串，替换掉为空白。
			str = str.replace(NumberUtils.need_replace_str_list_before[i],NumberUtils.need_replace_str_list_after[i]);
//			System.out.println(str); // 只 => 支
		}
		return str;
	}
	
	/**
	 * 正则提取返回结果
	 * 
	 * @param matchertext
	 *            正则表达式
	 * @param content
	 *            查找的文本内容
	 */

	public static String getMatcherResult(String matchertext, String content) {
		Pattern pattern = Pattern.compile(matchertext);
		Matcher matcher = pattern.matcher(content);
		StringBuffer bf = new StringBuffer(64);
		// while (matcher.find()) {
		if (matcher.find()) {
//			bf.append(matcher.group()).append(",");
			bf.append(matcher.group());
		}
		return bf.toString();
	}
	
	
	/**
	 * 递归函数 烟量 把包/天 统一转换成支/天
	 * @param str
	 * @return
	 */
	public static double transform_zhi(String str){
		double final_days = 0; // 声明 返回 最终的天数变量

		// 如果输入参数是空 则返回0
		if("".equals(str)){
			return 0;
		}

		/* 1 如果 有烟量单位的关键字 则 操作 ，然后调用递归，
		 * 需要把命中的烟量单位的下标 和 烟量单位字符串 在 烟量单位列表中的下标找到，
		 * 从转换到天的系数列表中得到系数
		 */
		int time_unit_index_in_str = -1;// 烟量单位在字符串中的下标
		int index_of_yan_liang_unit_2_zhi_list = -1; // 烟量单位 在 转换系数列表中的下标
		int time_unit_for_total_cnt = 0; // 烟量单位 当前遍历次数
		String str_before_time_unit = ""; // 烟量单位前的字符串
		double after_transform_number = 0; //  转换后的数字


		// 遍历 烟量单位
		for(String time_unit:NumberUtils.yan_liang_unit_list){
			time_unit_for_total_cnt++; // 烟量单位 当前遍历次数 +1
			// System.out.println("当前烟量单位="+time_unit);

			// 判断字符串中时候包含当前烟量单位字样
			if( str.contains( time_unit ) ){
				// System.out.println("当前字符串["+str+"] 包含了 烟量单位内容["+time_unit+"]");

				// 如果字符串 包含 当前烟量单位字样
				time_unit_index_in_str = str.indexOf(time_unit) ; // 烟量单位在字符串中的下标
				// System.out.println("烟量单位["+time_unit+"]在字符串["+str+"]中的下标="+time_unit_index_in_str);

				for(int i=0;i<NumberUtils.yan_liang_unit_list.length;i++){
					if (NumberUtils.yan_liang_unit_list[i] == time_unit){
						index_of_yan_liang_unit_2_zhi_list = i; // 烟量单位 在 转换系数列表中的下标 拿到了
						break;//退出for循环
					}
				}
				// System.out.println("烟量单位["+time_unit+"] 在 转换系数列表中的下标="+index_of_yan_liang_unit_2_zhi_list);
				str_before_time_unit = str.substring(0,time_unit_index_in_str);
				// System.out.println("烟量单位前的字符串="+烟量单位前的字符串);

				after_transform_number = str2number(str_before_time_unit);
				// System.out.println("转换后的数字="+转换后的数字);//转换后的数字=1

				str = str.substring(time_unit_index_in_str + time_unit.length()/*烟量单位字符串长度*/ /*截取到字符串末尾*/); // 剩下的需要再递归处理的字符串
				// System.out.println("剩下的需要处理的字符串="+str);//剩下的需要处理的字符串=3月

				// System.out.println("系数="+NumberUtils.yan_liang_unit_2_zhi_list[index_of_yan_liang_unit_2_zhi_list]); // 系数=365

				// 判断 年半 则 年前面的 转换后的数字 数字要加0.5
				if( time_unit.contains("半") ){
					after_transform_number = after_transform_number + 0.5;
				}
				// 再次调用递归函数
				final_days += after_transform_number * NumberUtils.yan_liang_unit_2_zhi_list[index_of_yan_liang_unit_2_zhi_list] + transform_zhi(str);
				// System.out.println("final_days="+final_days);
//				return final_days;
				break;
			}else{
				// 如果字符串 不包含 当前烟量单位字样
				index_of_yan_liang_unit_2_zhi_list++;// 烟量单位 在 转换系数列表中的下标+1
				// 如果烟量单位内容 是 列表中的最后一个
				if(time_unit_for_total_cnt == NumberUtils.yan_liang_unit_list.length){
					// 如果  遍历到最后 没有烟量单位的关键字 则 返回 0天
					return final_days;
				}
				}
			// return 100;
		}
		return final_days;
	}
	
	
	
	/**
	 * 函数_转换字符串为数字
	 * @param str 输入 需要转换为数字的字符串
	 * @return 转换后的字符串
	 */
	public static double str2number(String str){
		double result = 0; // 声明 字符串转换为数字的结果
		String after_append_str = ""; // 拼接后的字符串
		String str2NumberStr=""; // 单个字符转换为数字字符的结果
		boolean flag_stop_char = false; // 是否包含停止词

		// 循环遍历
		for(int i=0;i<str.length();i++){

			// 判断十的逻辑如下
			/*  十五年	105年	1	十在开始，后面有字符串，翻译成1
			 * 	   十年	10年		10	就1个十 翻译成10
			 *  二十年	200年	0	十在最后，翻译成0
			 */

			if( (str.charAt(i) == '十' || str.charAt(i) == '拾') && str.length() !=1 && i==0/*十是第1个字符*/ ){
				str2NumberStr = "1";
			}else if((str.charAt(i) == '十' || str.charAt(i) == '拾') && str.length() ==1){
				str2NumberStr = "10";
			}else if((str.charAt(i) == '十' || str.charAt(i) == '拾') && str.length() !=1 && i!=0/*十不是第1个字符*/){
				str2NumberStr = "0";
			}else{
				str2NumberStr = NumberUtils.numberCharCN2ArabString(str.charAt(i));
			}

			// 判断如果是 横线-  顿号、 则停止后面的转换
			for(int j=0;j< NumberUtils.stop_char.length;j++){
				if ( String.valueOf(str.charAt(i)).contains(NumberUtils.stop_char[j]) ){
					flag_stop_char = true;
					break; // 退出当前 判断是否包含停止词的for循环
				}
			}


			if(flag_stop_char){
				break;//退出 循环遍历 处理 翻译字符串的 for循环
			}else{
				// 如果 是 半 字样，则 前面 拼接后的字符串 转数字 +0.5后 再转字符串
				if(str.charAt(i) == '半'){
					// 如果 拼接后的字符串是空白 ，半是第1个字
					if( after_append_str=="" ){
						after_append_str = String.valueOf( 0.5 );
					}else{
						after_append_str = String.valueOf( Double.parseDouble(after_append_str)+0.5 );
					}
				}else{
					after_append_str+=str2NumberStr;
				}
			}

		}

		// 判断 拼接后的字符串长度，执行相应的字符串转数字操作。
		if(after_append_str.length()>0){ // 如果拼接后的字符串长度>0 ,则有内容，才能转换为数字，否则报错
			result = Double.valueOf(after_append_str);
		}else{  // 如果拼接后的字符串长度=0 ,则无内容，不能转换为数字，直接把0当作结果
			result = 0;
		}

		return result;
	}
	

}
