package cn.com.dhc.form;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyForm {
	//请求参数名必须与成员变量名一致
	private String companyId;
	private String companyName;
	private int companyType;
	private String address;
	private String zipCode;
	private String phone;
}
