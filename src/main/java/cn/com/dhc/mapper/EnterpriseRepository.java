package cn.com.dhc.mapper;

import java.util.List;
import java.util.Optional;

import cn.com.dhc.Entity.Enterprise;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import cn.com.dhc.Entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;


public interface EnterpriseRepository extends JpaRepository<Enterprise, Long> {
	Enterprise findByEmail(String email);


	boolean existsByEmail(String email);

    Enterprise findByEmailAndPassword(String email, String password);

    Enterprise findByEnterpriseId(Long enterpriseId);
}
