package com.imooc.passbook.dao;

import com.imooc.passbook.entity.Merchants;
import org.springframework.data.jpa.repository.JpaRepository;

//利用springboot-starter-jpa 将java对象映射到sql的记录，提供便捷的方法，运行时自动生成sql语句完成查询等操作
public interface MerchantsDao extends JpaRepository<Merchants,Integer> {
    /**
     * <h2>根据 id 获取商户对象</h2>
     * @param id 商户 id
     * @return {@link Merchants}
     * */
    Merchants findById(Integer id);

    /**
     * <h2>根据商户名称获取商户对象</h2>
     * @param name 商户名称
     * @return {@link Merchants}
     * */
    Merchants findByName(String name);
}
