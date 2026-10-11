package cn.com.dhc.form;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonForm {
	private String personId;
	private int documentType;
	private String documentId;
	private String personName;
	private String sex;
	private int nation;
	private String birthday;
	private String workDate;
	private String retireDate;
	private int retireStatus;
	private int residentType;
	private String residentLocation;
	private int educationLevel;
	private int politicalFace;
	private int personIdentity;
	private int employmentForm;
	private int technicalPosition;
	private int workerTechnicalLevel;
	private int maritalStatus;
	private int administrativePosition;
	private String notes;
	private String unitCode;
	private int medicalPersonCategory;
	private int healthStatus;
	private int laborModelLogo;
	private int cadreLogo;
	private int civilServantLogo;
	private int inserviceLogo;
	private int residentCategory;
	private int flexibleEmploymentSign;
	private int migrantWorkerLogo;
	private int employerLogo;
	private int militaryPersonLogo;
	private String socialSecurityCardId;
	private String designatedMedicalInstitutionCode;
}
