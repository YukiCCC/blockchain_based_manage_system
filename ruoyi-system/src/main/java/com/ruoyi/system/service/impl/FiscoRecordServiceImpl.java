package com.ruoyi.system.service.impl;

import com.ruoyi.system.fisco.vo.Fiscovo;
import com.ruoyi.system.service.IFiscoRecordService;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.transaction.manager.AssembleTransactionProcessor;
import org.fisco.bcos.sdk.transaction.manager.TransactionProcessorFactory;
import org.fisco.bcos.sdk.transaction.model.dto.TransactionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

@Service
public class FiscoRecordServiceImpl implements IFiscoRecordService {
    
    private static final Logger log = LoggerFactory.getLogger(FiscoRecordServiceImpl.class);
    
    // 获取配置文件路径
    private static final String CONFIG_FILE = "ruoyi-system/src/main/resources/config-example.toml";
    
    // 合约地址 - 注意：这个地址需要根据实际部署的合约地址进行修改
    private static final String CONTRACT_ADDRESS = "0x7894a9e1947e91aa2246b3d80c5c8abf6221d664";
    
    // 成功标识
    private static final String SUCCESS = "1";
    
    /**
     * 添加区块链记录
     *
     * @param recordId 记录ID
     * @param description 描述信息
     * @param remark 备注信息
     * @return 是否添加成功
     */
    @Override
    public boolean addFiscoRecord(String recordId, String description, String remark) {
        BcosSDK sdk = null;
        try {
            // 初始化BcosSDK对象
            sdk = BcosSDK.build(CONFIG_FILE);
            // 获取Client对象，此处传入的群组ID为1
            Client client = sdk.getClient(1);
            // 构造AssembleTransactionProcessor对象
            CryptoKeyPair keyPair = client.getCryptoSuite().createKeyPair();
            AssembleTransactionProcessor transactionProcessor = TransactionProcessorFactory
                .createAssembleTransactionProcessor(client, keyPair, "ruoyi-system/src/main/resources/abi/", "");
            
            // 创建调用交易函数的参数
            List<Object> params = new ArrayList<>();
            params.add(recordId);
            params.add(description);
            params.add(remark);
            
            log.info("开始添加区块链记录，recordId: {}, description: {}, remark: {}", recordId, description, remark);
            
            // 调用record合约的addRecord方法
            TransactionResponse transactionResponse = transactionProcessor
                .sendTransactionAndGetResponseByContractLoader("Record", CONTRACT_ADDRESS, "addRecord", params);
            
            // 检查交易状态
            if (!"0x0".equals(transactionResponse.getTransactionReceipt().getStatus())) {
                log.error("添加记录失败，交易状态: {}", transactionResponse.getTransactionReceipt().getStatus());
                return false;
            }
            
            // 获取返回值
            List<Object> returnValues = transactionResponse.getReturnObject();
            if (returnValues != null && !returnValues.isEmpty()) {
                String response = returnValues.get(0).toString();
                log.info("添加记录返回值: {}", response);
                return SUCCESS.equals(response);
            }
            
            log.warn("添加记录无返回值");
            return false;
            
        } catch (Exception e) {
            log.error("添加区块链记录异常", e);
            return false;
        }
    }
    
    /**
     * 获取区块链记录
     *
     * @param recordId 记录ID
     * @return 记录信息
     */
    @Override
    public Fiscovo getFiscoRecord(String recordId) {
        BcosSDK sdk = null;
        try {
            // 初始化BcosSDK对象
            sdk = BcosSDK.build(CONFIG_FILE);
            // 获取Client对象，此处传入的群组ID为1
            Client client = sdk.getClient(1);
            // 构造AssembleTransactionProcessor对象
            CryptoKeyPair keyPair = client.getCryptoSuite().createKeyPair();
            AssembleTransactionProcessor transactionProcessor = TransactionProcessorFactory
                .createAssembleTransactionProcessor(client, keyPair, "ruoyi-system/src/main/resources/abi/", "");
            
            // 调用合约查询接口
            List<Object> params = new ArrayList<>();
            params.add(recordId);
            
            log.info("开始查询区块链记录，recordId: {}", recordId);
            
            // 调用record合约的getRecord方法
            TransactionResponse transactionResponse = transactionProcessor
                .sendTransactionAndGetResponseByContractLoader("Record", CONTRACT_ADDRESS, "getRecord", params);
            
            // 检查交易状态
            if (!"0x0".equals(transactionResponse.getTransactionReceipt().getStatus())) {
                log.error("查询记录失败，交易状态: {}", transactionResponse.getTransactionReceipt().getStatus());
                return new Fiscovo();
            }
            
            // 解析返回值
            List<Object> returnValues = transactionResponse.getReturnObject();
            Fiscovo fiscovo = new Fiscovo();
            
            if (returnValues != null && returnValues.size() == 2) {
                fiscovo.setDescription((String) returnValues.get(0));
                fiscovo.setRemark((String) returnValues.get(1));
                log.info("查询记录成功，description: {}, remark: {}", fiscovo.getDescription(), fiscovo.getRemark());
            } else {
                log.warn("查询记录返回值长度不正确，期望2个，实际: {}", 
                    returnValues != null ? returnValues.size() : 0);
            }
            
            return fiscovo;
            
        } catch (Exception e) {
            log.error("查询区块链记录异常", e);
            return new Fiscovo();
        }
    }
}
