package com.ruoyi.system.service;

import com.ruoyi.system.fisco.vo.Fiscovo;
import com.ruoyi.system.fisco.vo.SysArchiveFiscoVO;
import com.ruoyi.system.fisco.domain.SysArchiveFisco;
import java.util.List;

/**
 * FISCO区块链记录服务接口
 *
 * @author ruoyi
 */
public interface IFiscoRecordService {
    
    /**
     * 添加区块链记录
     *
     * @param recordId 记录ID
     * @param description 描述信息
     * @param remark 备注信息
     * @return 是否添加成功
     */
    boolean addFiscoRecord(String recordId, String description, String remark);
    
    /**
     * 获取区块链记录
     *
     * @param recordId 记录ID
     * @return 记录信息
     */
    Fiscovo getFiscoRecord(String recordId);
    
    /**
     * 添加档案
     *
     * @param sysArchiveFiscoVO 档案信息VO
     * @return 是否添加成功
     */
    boolean addArchiveFisco(SysArchiveFiscoVO sysArchiveFiscoVO);
    
    /**
     * 查询所有档案信息
     *
     * @return 档案列表
     */
    List<SysArchiveFisco> listAllArchiveFisco();
}