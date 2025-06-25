package dev.mvc.member;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MemberService {
  @Autowired
  private MemberDAOInter memberDAO;

  // ID 중복 검사
  public int checkID(String id) {
    return memberDAO.checkID(id);
  }

  // 일반 회원 가입
  public int create(MemberVO memberVO) {
    return memberDAO.create(memberVO);
  }

  // 공급자 회원 가입
  public int insertMember(MemberVO memberVO) {
    memberVO.setSupplier_approved("N"); // 초기 승인 상태 'N'
    return memberDAO.insertMember(memberVO);
  }

  // 승인 대기 공급자 목록 조회
  public List<MemberVO> getPendingSuppliers() {
    return memberDAO.selectPendingSuppliers();
  }

  // 공급자 승인 처리
  public int approveSupplier(int memberno) {
    Map<String, Object> paramMap = new HashMap<>();
    paramMap.put("memberno", memberno);
    paramMap.put("supplier_approved", "Y");

    int cnt1 = memberDAO.updateSupplierApproved(paramMap);
    int cnt2 = memberDAO.updateGrade(memberno, 10);
    return (cnt1 + cnt2) / 2;
  }


  //공급자 승인 거절 처리
  public int rejectSupplier(int memberno) {
   Map<String, Object> paramMap = new HashMap<>();
   paramMap.put("memberno", memberno);
   paramMap.put("supplier_approved", "R"); // 거절
   return memberDAO.updateSupplierApproved(paramMap);
  }
  
  //승인 취소 (대기 상태로 변경)
  public int cancelSupplierApproval(int memberno) {
   Map<String, Object> paramMap = new HashMap<>();
   paramMap.put("memberno", memberno);
   paramMap.put("supplier_approved", "N"); // 대기로
   int cnt1 = memberDAO.updateSupplierApproved(paramMap);
  
   int cnt2 = memberDAO.updateGrade(memberno, 5); // 다시 대기등급
   return (cnt1 + cnt2) / 2;
  }
  
  public String uploadFile(String uploadDir, MultipartFile file) throws Exception {
    if (file.isEmpty()) {
        return null;
    }

    File dir = new File(uploadDir);
    if (!dir.exists()) dir.mkdirs();

    String originalName = file.getOriginalFilename();
    String ext = originalName.substring(originalName.lastIndexOf("."));
    String saveFileName = UUID.randomUUID().toString() + ext;

    File saveFile = new File(dir, saveFileName);
    file.transferTo(saveFile);

    return saveFileName;
}

}
