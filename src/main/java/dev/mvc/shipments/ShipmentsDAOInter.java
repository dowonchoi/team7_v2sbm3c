package dev.mvc.shipments;

import java.util.List;

public interface ShipmentsDAOInter {
  
  public List<ShipmentsVO> list();
  
  public ShipmentsVO read(int shipmentno);
  
  public int updateStatus(ShipmentsVO shipmentVO);
  
  public int create(ShipmentsVO shipmentVO);
}