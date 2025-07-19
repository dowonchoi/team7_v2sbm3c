package dev.mvc.mms_img;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Mapper
public interface MmsSendLogDAOInter {
    int create(MmsSendLogVO vo);
    List<MmsSendLogVO> listByImgno(int mimgno);
}

