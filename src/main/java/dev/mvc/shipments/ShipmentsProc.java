package dev.mvc.shipments;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("dev.mvc.shipments.ShipmentsProc")
public class ShipmentsProc implements ShipmentsProcInter {

  @Autowired
  private ShipmentsDAOInter shipmentDAO;

  @Override
  public List<ShipmentsVO> list() {
    return shipmentDAO.list();
  }

  @Override
  public ShipmentsVO read(int shipmentno) {
    return shipmentDAO.read(shipmentno);
  }

  @Override
  public int updateStatus(ShipmentsVO shipmentVO) {
    return shipmentDAO.updateStatus(shipmentVO);
  }

  @Override
  public int create(ShipmentsVO shipmentVO) {
    return shipmentDAO.create(shipmentVO);
  }

}
