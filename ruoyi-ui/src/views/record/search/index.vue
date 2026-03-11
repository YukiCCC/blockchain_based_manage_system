<template>
  <div class="app-container">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>链上数据查询</span>
      </div>
      
      <!-- 搜索区域 -->
      <el-row :gutter="20" style="margin-bottom: 20px;">
        <el-col :span="12">
          <el-input v-model="searchId" placeholder="请输入记录ID" clearable @keyup.enter.native="handleSearch">
            <el-button slot="append" icon="el-icon-search" @click="handleSearch" :loading="loading">查询</el-button>
          </el-input>
        </el-col>
      </el-row>

      <!-- 结果展示区域 -->
      <div v-if="chainInfo">
        <el-divider content-position="left">查询结果</el-divider>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="记录ID">{{ searchId }}</el-descriptions-item>
          <el-descriptions-item label="描述信息">
            <div v-if="parsedDescription">
              <el-descriptions :column="2" size="small" border>
                <el-descriptions-item label="开始时间">{{ parsedDescription.startTime }}</el-descriptions-item>
                <el-descriptions-item label="结束时间">{{ parsedDescription.endTime }}</el-descriptions-item>
                <el-descriptions-item label="部门">{{ parsedDescription.department }}</el-descriptions-item>
                <el-descriptions-item label="数据">{{ parsedDescription.inspectionLine }}</el-descriptions-item>
                <el-descriptions-item label="是否异常">
                  <el-tag :type="parsedDescription.isAbnormal === '是' ? 'danger' : 'success'">
                    {{ parsedDescription.isAbnormal }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="数据类型">
                  <el-tag :type="parsedDescription.hasEquipmentDamage === '是' ? 'danger' : 'success'">
                    {{ parsedDescription.hasEquipmentDamage === '是' ? '无人机命令集' : '无人机查勘数据' }}
                  </el-tag>
                </el-descriptions-item>
              </el-descriptions>
            </div>
            <div v-else>
              <pre style="white-space: pre-wrap; word-wrap: break-word; background-color: #f5f5f5; padding: 10px; border-radius: 4px;">{{ chainInfo.description }}</pre>
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="备注信息" v-if="chainInfo.remark">{{ chainInfo.remark }}</el-descriptions-item>
        </el-descriptions>
        
        <div style="margin-top: 20px; text-align: center;">
          <el-button type="primary" icon="el-icon-document-copy" @click="copyChainInfo">复制信息</el-button>
          <el-button icon="el-icon-delete" @click="clearResult">清空结果</el-button>
        </div>
      </div>
      
      <el-empty v-else description="请输入记录ID并点击查询按钮"></el-empty>
    </el-card>
  </div>
</template>

<script>
import { getRecord } from "@/api/fisco/record";

export default {
  name: "ArchiveSearch",
  data() {
    return {
      // 加载状态
      loading: false,
      // 搜索ID
      searchId: "",
      // 链上信息数据
      chainInfo: null,
      // 解析后的描述信息
      parsedDescription: null
    };
  },
  methods: {
    /** 搜索按钮点击 */
    handleSearch() {
      if (!this.searchId) {
        this.$modal.msgWarning("请输入记录ID");
        return;
      }
      this.getChainInfo(this.searchId);
    },

    /** 获取链上信息 */
    async getChainInfo(id) {
      this.loading = true;
      this.chainInfo = null;
      this.parsedDescription = null;
      
      try {
        // 调用接口获取链上信息
        const response = await getRecord(id);
        
        if (!response.data) {
             this.$modal.msgError("未找到该记录");
             return;
        }

        this.chainInfo = response.data;
        
        // 解析描述信息
        this.parseDescription();
        
        console.log("获取链上信息成功:", this.chainInfo);
        this.$modal.msgSuccess("查询成功");
      } catch (error) {
        console.error("获取链上信息失败:", error);
        this.$modal.msgError("获取链上信息失败：" + (error.message || "未知错误"));
      } finally {
        this.loading = false;
      }
    },

    /** 解析描述信息 */
    parseDescription() {
      try {
        if (this.chainInfo && this.chainInfo.description) {
          // 尝试判断是否是JSON字符串
          if (typeof this.chainInfo.description === 'string') {
             try {
                this.parsedDescription = JSON.parse(this.chainInfo.description);
             } catch (e) {
                // 解析失败，说明不是JSON，直接显示原文本
                this.parsedDescription = null;
             }
          } else if (typeof this.chainInfo.description === 'object') {
            this.parsedDescription = this.chainInfo.description;
          }
        } else {
          this.parsedDescription = null;
        }
      } catch (e) {
        console.warn("描述信息解析异常:", e);
        this.parsedDescription = null;
      }
    },

    /** 复制链上信息 */
    copyChainInfo() {
      let info = `记录ID: ${this.searchId}\n`;
      
      if (this.parsedDescription) {
        info += `描述信息:
开始时间: ${this.parsedDescription.startTime || ''}
结束时间: ${this.parsedDescription.endTime || ''}
部门: ${this.parsedDescription.department || ''}
检修线路: ${this.parsedDescription.inspectionLine || ''}
是否异常: ${this.parsedDescription.isAbnormal || ''}
设备是否损坏: ${this.parsedDescription.hasEquipmentDamage || ''}`;
      } else {
        info += `描述信息: ${this.chainInfo.description || ''}`;
      }
      
      if (this.chainInfo.remark) {
        info += `\n备注信息: ${this.chainInfo.remark}`;
      }
      
      const textarea = document.createElement('textarea');
      textarea.value = info;
      document.body.appendChild(textarea);
      textarea.select();
      document.execCommand('copy');
      document.body.removeChild(textarea);
      this.$message.success('链上信息已复制到剪贴板');
    },
    
    /** 清空结果 */
    clearResult() {
        this.chainInfo = null;
        this.parsedDescription = null;
        this.searchId = "";
    }
  }
};
</script>

<style scoped>
.box-card {
  width: 100%;
  min-height: 500px;
}
</style>