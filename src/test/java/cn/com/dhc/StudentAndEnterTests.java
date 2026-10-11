package cn.com.dhc;

import java.util.List;
import java.util.Optional;

import cn.com.dhc.Entity.Enterprise;
import cn.com.dhc.mapper.EnterpriseRepository;
import cn.com.dhc.mapper.StudentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import cn.com.dhc.Entity.Student;

@SpringBootTest
class StudentAndEnterTests {
	@Autowired
	private StudentRepository companyMapper;
	@Test
	void one() {

		System.out.println("11111");

	}

}