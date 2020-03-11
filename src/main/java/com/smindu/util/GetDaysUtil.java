package com.smindu.util;

import org.apache.hadoop.hive.ql.exec.UDF;

public class GetDaysUtil extends UDF {

	// 不带调试打印语句

	public static void main(String[] args) {
//
//		String[] 输入 = {"3月"};
//
//		for(int i=0;i<输入.length;i++){
//			System.out.println(输入[i]+"#"+getDays(输入[i]));
//		}
//
		System.out.println(evaluate("小半年"));
////		System.out.println(getDays("一个余年三个月")); // 455 ok
////		System.out.println(getDays("十二个余年"));
//
	}


	/**
	 * 2 定义 递归函数
	 * 如果 有年 则 把年和年左边的替换为空白的结果 ，传递到递归参数中，递归终止条件是不再包含时间单位的关键字。
	 * 时间单位左边的结果 调用 函数_解析为数字 并把结果乘以 时间单位对应的天的系数，
	 * @param str 输入 需要转换的字符串
	 * @return 转换后的天数 整数
	 */

	public static double transform_days(String str){
		double final_days = 0; // 声明 返回 最终的天数变量

		// 如果输入参数是空 则返回0
		if("".equals(str)){
			return 0;
		}

		/* 1 如果 有时间单位的关键字 则 操作 ，然后调用递归，
		 * 需要把命中的时间单位的下标 和 时间单位字符串 在 时间单位列表中的下标找到，
		 * 从转换到天的系数列表中得到系数
		 */
		int time_unit_index_in_str = -1;// 时间单位在字符串中的下标
		int index_of_time_unit_2_day_list = -1; // 时间单位 在 转换系数列表中的下标
		int time_unit_for_total_cnt = 0; // 时间单位 当前遍历次数
		String str_before_time_unit = ""; // 时间单位前的字符串
		double after_transform_number = 0; //  转换后的数字


		// 遍历 时间单位
		for(String time_unit:NumberUtils.time_unit_list){
			time_unit_for_total_cnt++; // 时间单位 当前遍历次数 +1
			// System.out.println("当前时间单位="+time_unit);

			// 判断字符串中时候包含当前时间单位字样
			if( str.contains( time_unit ) ){
				// System.out.println("当前字符串["+str+"] 包含了 时间单位内容["+time_unit+"]");

				// 如果字符串 包含 当前时间单位字样
				time_unit_index_in_str = str.indexOf(time_unit) ; // 时间单位在字符串中的下标
				// System.out.println("时间单位["+time_unit+"]在字符串["+str+"]中的下标="+time_unit_index_in_str);

				for(int i=0;i<NumberUtils.time_unit_list.length;i++){
					if (NumberUtils.time_unit_list[i] == time_unit){
						index_of_time_unit_2_day_list = i; // 时间单位 在 转换系数列表中的下标 拿到了
						break;//退出for循环
					}
				}
				// System.out.println("时间单位["+time_unit+"] 在 转换系数列表中的下标="+index_of_time_unit_2_day_list);
				str_before_time_unit = str.substring(0,time_unit_index_in_str);
				// System.out.println("时间单位前的字符串="+时间单位前的字符串);

				after_transform_number = str2number(str_before_time_unit);
				// System.out.println("转换后的数字="+转换后的数字);//转换后的数字=1

				str = str.substring(time_unit_index_in_str + time_unit.length()/*时间单位字符串长度*/ /*截取到字符串末尾*/); // 剩下的需要再递归处理的字符串
				// System.out.println("剩下的需要处理的字符串="+str);//剩下的需要处理的字符串=3月

				// System.out.println("系数="+NumberUtils.time_unit_2_day_list[index_of_time_unit_2_day_list]); // 系数=365

				// 判断 年半 则 年前面的 转换后的数字 数字要加0.5
				if( time_unit.contains("半") ){
					after_transform_number = after_transform_number + 0.5;
				}
				// 再次调用递归函数
				final_days += after_transform_number * NumberUtils.time_unit_2_day_list[index_of_time_unit_2_day_list] + transform_days(str);
				// System.out.println("final_days="+final_days);
//				return final_days;
				break;
			}else{
				// 如果字符串 不包含 当前时间单位字样
				index_of_time_unit_2_day_list++;// 时间单位 在 转换系数列表中的下标+1
				// 如果时间单位内容 是 列表中的最后一个
				if(time_unit_for_total_cnt == NumberUtils.time_unit_list.length){
					// 如果  遍历到最后 没有时间单位的关键字 则 返回 0天
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
		double int_temp = 0.0; // 字符串 转换为数字的结果

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
						try {
							int_temp = Double.parseDouble(after_append_str);
						} catch (NumberFormatException e) {
							// TODO Auto-generated catch block
							//e.printStackTrace();
							int_temp = 0.0;
						}
						after_append_str = String.valueOf( int_temp+0.5 );
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

	/**
	 * 主要 方法 通过V2字段中的值 得到对应的天数
	 * @param str 传入的V2字段值
	 * @return 转换后的天数
	 */
	public static String evaluate(String str){
		String result = null; // 声明返回结果变量

		// 1 循环 调用 替换方法 根据替换列表中的每个字符串，替换掉为空白。
		str = replace2Blank(str);
		// System.out.println("替换指定内容为空白后的字符串="+str); // 替换指定内容为空白后的字符串=1年

		// 2 定义 递归函数
		result = String.valueOf(transform_days(str));

		return result;
	}


	/**
	 * 循环 调用 替换方法 根据替换列表中的每个字符串，替换掉为空白。
	 * @param str 传入的字符串
	 * @return 返回替换指定字符串 为空白 后的结果
	 */
	public static String replace2Blank(String str) {
		// 1 循环 调用 替换方法 根据替换列表中的每个字符串，替换掉为空白。
		for(String replace_str:NumberUtils.need_replace_blank_list){
//			System.out.println(replace_str); // 个, 余

			// 根据替换列表中的每个字符串，替换掉为空白。
			str = str.replace(replace_str,"");
//			System.out.println(str); // 1个余年 => 1年
		}

		return str;
	}

}
