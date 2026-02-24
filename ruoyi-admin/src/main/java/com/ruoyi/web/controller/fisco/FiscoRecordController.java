package com.ruoyi.web.controller.fisco;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.fisco.vo.Fiscovo;
import com.ruoyi.system.service.IFiscoRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * FISCO区块链记录控制器
 *
 * @author ruoyi
 */
@Api("FISCO区块链记录管理")
@RestController
@RequestMapping("/fisco/record")
@Validated
public class FiscoRecordController extends BaseController {

    @Autowired
    private IFiscoRecordService fiscoRecordService;

    /**
     * 添加区块链记录
     */
    @ApiOperation("添加区块链记录")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "recordId", value = "记录ID", required = true, dataType = "String", paramType = "form"),
        @ApiImplicitParam(name = "description", value = "描述信息", required = true, dataType = "String", paramType = "form"),
        @ApiImplicitParam(name = "remark", value = "备注信息", required = true, dataType = "String", paramType = "form")
    })
    @Log(title = "FISCO区块链记录", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('fisco:record:add')")
    @PostMapping("/add")
    public AjaxResult addRecord(@RequestParam("recordId") String recordId,
                               @RequestParam("description") String description,
                               @RequestParam("remark") String remark) {
        try {
            // 参数校验
            if (recordId == null || recordId.trim().isEmpty()) {
                return error("记录ID不能为空");
            }
            if (description == null || description.trim().isEmpty()) {
                return error("描述信息不能为空");
            }
            if (remark == null || remark.trim().isEmpty()) {
                return error("备注信息不能为空");
            }

            logger.info("开始添加区块链记录，recordId: {}, description: {}, remark: {}", 
                       recordId, description, remark);

            boolean result = fiscoRecordService.addFiscoRecord(recordId.trim(), description.trim(), remark.trim());
            
            if (result) {
                logger.info("添加区块链记录成功，recordId: {}", recordId);
                return success("添加记录成功");
            } else {
                logger.warn("添加区块链记录失败，recordId: {}", recordId);
                return error("添加记录失败，请检查区块链网络连接或记录是否已存在");
            }
        } catch (Exception e) {
            logger.error("添加区块链记录异常，recordId: " + recordId, e);
            return error("添加记录异常：" + e.getMessage());
        }
    }

    /**
     * 获取区块链记录
     */
    @ApiOperation("获取区块链记录")
    @ApiImplicitParam(name = "recordId", value = "记录ID", required = true, dataType = "String", paramType = "path")
    @Log(title = "FISCO区块链记录", businessType = BusinessType.OTHER)
    @PreAuthorize("@ss.hasPermi('fisco:record:query')")
    @GetMapping("/get/{recordId}")
    public AjaxResult getRecord(@PathVariable("recordId") String recordId) {
        try {
            // 参数校验
            if (recordId == null || recordId.trim().isEmpty()) {
                return error("记录ID不能为空");
            }

            logger.info("开始查询区块链记录，recordId: {}", recordId);

            Fiscovo record = fiscoRecordService.getFiscoRecord(recordId.trim());
            
            if (record != null && (record.getDescription() != null || record.getRemark() != null)) {
                logger.info("查询区块链记录成功，recordId: {}", recordId);
                return success(record);
            } else {
                logger.warn("未找到区块链记录，recordId: {}", recordId);
                return error("未找到指定记录");
            }
        } catch (Exception e) {
            logger.error("查询区块链记录异常，recordId: " + recordId, e);
            return error("查询记录异常：" + e.getMessage());
        }
    }

    /**
     * 批量添加区块链记录
     */
    @ApiOperation("批量添加区块链记录")
    @Log(title = "FISCO区块链记录", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('fisco:record:add')")
    @PostMapping("/addBatch")
    public AjaxResult addRecordBatch(@RequestBody FiscoRecordBatchRequest request) {
        try {
            // 参数校验
            if (request == null || request.getRecords() == null || request.getRecords().isEmpty()) {
                return error("批量添加记录不能为空");
            }

            logger.info("开始批量添加区块链记录，数量: {}", request.getRecords().size());

            int successCount = 0;
            int failCount = 0;
            StringBuilder errorMessages = new StringBuilder();

            for (FiscoRecordRequest record : request.getRecords()) {
                try {
                    if (record.getRecordId() == null || record.getRecordId().trim().isEmpty() ||
                        record.getDescription() == null || record.getDescription().trim().isEmpty() ||
                        record.getRemark() == null || record.getRemark().trim().isEmpty()) {
                        failCount++;
                        errorMessages.append("记录ID: ").append(record.getRecordId()).append(" 参数不完整; ");
                        continue;
                    }

                    boolean result = fiscoRecordService.addFiscoRecord(
                        record.getRecordId().trim(), 
                        record.getDescription().trim(), 
                        record.getRemark().trim()
                    );
                    
                    if (result) {
                        successCount++;
                    } else {
                        failCount++;
                        errorMessages.append("记录ID: ").append(record.getRecordId()).append(" 添加失败; ");
                    }
                } catch (Exception e) {
                    failCount++;
                    errorMessages.append("记录ID: ").append(record.getRecordId()).append(" 异常: ").append(e.getMessage()).append("; ");
                }
            }

            logger.info("批量添加区块链记录完成，成功: {}, 失败: {}", successCount, failCount);

            if (failCount == 0) {
                return success("批量添加记录成功，共添加 " + successCount + " 条记录");
            } else if (successCount == 0) {
                return error("批量添加记录全部失败：" + errorMessages.toString());
            } else {
                return warn("批量添加记录部分成功，成功: " + successCount + " 条，失败: " + failCount + " 条。失败原因：" + errorMessages.toString());
            }
        } catch (Exception e) {
            logger.error("批量添加区块链记录异常", e);
            return error("批量添加记录异常：" + e.getMessage());
        }
    }

    /**
     * 检查记录是否存在
     */
    @ApiOperation("检查记录是否存在")
    @ApiImplicitParam(name = "recordId", value = "记录ID", required = true, dataType = "String", paramType = "path")
    @Log(title = "FISCO区块链记录", businessType = BusinessType.OTHER)
    @PreAuthorize("@ss.hasPermi('fisco:record:query')")
    @GetMapping("/exists/{recordId}")
    public AjaxResult checkRecordExists(@PathVariable("recordId") String recordId) {
        try {
            // 参数校验
            if (recordId == null || recordId.trim().isEmpty()) {
                return error("记录ID不能为空");
            }

            logger.info("开始检查区块链记录是否存在，recordId: {}", recordId);

            Fiscovo record = fiscoRecordService.getFiscoRecord(recordId.trim());
            boolean exists = record != null && (record.getDescription() != null || record.getRemark() != null);

            logger.info("检查区块链记录完成，recordId: {}, exists: {}", recordId, exists);

            return success("检查完成");
        } catch (Exception e) {
            logger.error("检查区块链记录异常，recordId: " + recordId, e);
            return error("检查记录异常：" + e.getMessage());
        }
    }

    /**
     * 单个记录请求对象
     */
    public static class FiscoRecordRequest {
        private String recordId;
        private String description;
        private String remark;

        // Getters and Setters
        public String getRecordId() {
            return recordId;
        }

        public void setRecordId(String recordId) {
            this.recordId = recordId;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }
    }

    /**
     * 批量记录请求对象
     */
    public static class FiscoRecordBatchRequest {
        private java.util.List<FiscoRecordRequest> records;

        public java.util.List<FiscoRecordRequest> getRecords() {
            return records;
        }

        public void setRecords(java.util.List<FiscoRecordRequest> records) {
            this.records = records;
        }
    }
}
