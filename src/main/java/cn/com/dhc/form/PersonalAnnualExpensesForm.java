package cn.com.dhc.form;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonalAnnualExpensesForm {
	private String personId;
	private String annual;
	private String annualReimbursementFrequency;
	private String annualMedicalExpense;
	private String medicalCenterReimbursement;
	private String personalExpenses;

}
