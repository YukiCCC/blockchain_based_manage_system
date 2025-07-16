package com.ruoyi.system.service.impl;

import com.ruoyi.system.fisco.vo.Fiscovo;
import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.transaction.manager.AssembleTransactionProcessor;
import org.fisco.bcos.sdk.transaction.manager.TransactionProcessorFactory;
import org.fisco.bcos.sdk.transaction.model.dto.TransactionResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * 系统访问WeBASE-FISCO 服务层处理
 *
 * @author ruoyi
 */
public class SysFiscoServiceImpl {

    //获取配置文件路径
    public static final String configFile = "ruoyi-system/src/main/resources/config-example.toml";

    public static final String SUCCESS = "1";
    public static boolean addFiscoRecord(String[] args)throws Exception {
        //初始化BcosSDK对象
        BcosSDK sdk = BcosSDK.build(configFile);
        //获取Client对象，此处传入的群组ID为1
        Client client = sdk.getClient(Integer.valueOf(1));
        // 构造AssembleTransactionProcessor对象，需要传入client对象，CryptoKeyPair对象和abi、binary文件存放的路径。abi和binary文件需要在上一步复制到定义的文件夹中。
        CryptoKeyPair keyPair = client.getCryptoSuite().createKeyPair();
        //只交易和查询，不部署合约
        AssembleTransactionProcessor transactionProcessor = TransactionProcessorFactory.createAssembleTransactionProcessor(client, keyPair, "ruoyi-system/src/main/resources/abi/", "");

        //同步方式发送交易
        //创建调用交易函数的参数
        String recordId = "4";
        String description = "UAV-0002-徐州丰县-0714";
        String remark = "3b7a6d2e-8c5f-49d1-9e7b-2a3c4d5e6f78";

        List<Object> params = new ArrayList<>();
        params.add(recordId);
        params.add(description);
        params.add(remark);

        //调用record合约，调用的数名为[addRecord]，函数参数类型为params
        TransactionResponse transactionResponse = transactionProcessor.sendTransactionAndGetResponseByContractLoader("Record", "0x732aabd91a8ef870634ab9adedf972959297b615", "addRecord", params);
        String response = new String();
        //打印返回值
        List<Object> returnValues = transactionResponse.getReturnObject();
        if(returnValues != null){
            for(Object value :returnValues) {
                System.out.println("上链返回值:" + value.toString());
                response = value.toString();
                break;
            }
        }
        if(response==SUCCESS){
            return true;
        }else{
            return false;
        }
    }

    public static Fiscovo getFiscoRecord(String Id)throws Exception {
        //初始化BcosSDK对象
        BcosSDK sdk = BcosSDK.build(configFile);
        //获取Client对象，此处传入的群组ID为1
        Client client = sdk.getClient(Integer.valueOf(1));
        // 构造AssembleTransactionProcessor对象，需要传入client对象，CryptoKeyPair对象和abi、binary文件存放的路径。abi和binary文件需要在上一步复制到定义的文件夹中。
        CryptoKeyPair keyPair = client.getCryptoSuite().createKeyPair();
        //只交易和查询，不部署合约
        AssembleTransactionProcessor transactionProcessor = TransactionProcessorFactory.createAssembleTransactionProcessor(client, keyPair, "ruoyi-system/src/main/resources/abi/", "");

        //调用合约查询接口
        List<Object> params2 = new ArrayList<>();
        params2.add(Id);
        // 调用record合约的「getRecord」的数，参数为recordid
        TransactionResponse transactionResponse2 = transactionProcessor.sendTransactionAndGetResponseByContractLoader("Record", "0x732aabd91a8ef870634ab9adedf972959297b615", "getRecord", params2);
        //打印返国值
        List<Object> returnValues2 = transactionResponse2.getReturnObject();
        Fiscovo response = new Fiscovo();
        if(returnValues2 != null) {
            // 检查返回值的长度是否正确
            if (returnValues2.size() == 2) {
                response.setDescription((String) returnValues2.get(0));
                response.setRemark ((String) returnValues2.get(1));
                return response;
            } else {
                System.out.println("返回值长度不正确");
            }
        }
        return response;
    }
}
