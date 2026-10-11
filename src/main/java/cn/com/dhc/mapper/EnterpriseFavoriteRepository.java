package cn.com.dhc.mapper;

import cn.com.dhc.Entity.Enterprise;
import cn.com.dhc.Entity.EnterpriseFavorite;
import cn.com.dhc.Entity.EnterpriseFavoriteId;
import cn.com.dhc.Entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnterpriseFavoriteRepository extends JpaRepository<EnterpriseFavorite, EnterpriseFavoriteId> {

    // 检查是否已收藏
    boolean existsByEnterpriseAndStudent(Enterprise enterprise, Student student);

    // 获取企业收藏列表
    List<EnterpriseFavorite> findByEnterprise(Enterprise enterprise);

    // 删除收藏
    void deleteByEnterpriseAndStudent(Enterprise enterprise, Student student);
}