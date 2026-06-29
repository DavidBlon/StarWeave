package com.starweave.mapper;

import com.starweave.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {

    User findById(@Param("id") Long id);

    User findByNickname(@Param("nickname") String nickname);

    User findByUsername(@Param("username") String username);

    List<User> findAll();

    int insert(User user);

    int update(User user);

    int updateBorderStyle(@Param("id") Long id, @Param("borderStyle") String borderStyle);

    int incrementTokenVersion(@Param("id") Long id);

    int setSponsor(@Param("id") Long id, @Param("isSponsor") Boolean isSponsor);

    int deleteById(@Param("id") Long id);
}
