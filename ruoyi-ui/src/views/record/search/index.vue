<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-refresh"
          size="mini"
          @click="getList"
        >刷新
        </el-button>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="archiveList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="档案编号" align="center" prop="archiveId" />
      <el-table-column label="姓名" align="center" prop="name" :show-overflow-tooltip="true" />
      <el-table-column label="工号" align="center" prop="employeeId" :show-overflow-tooltip="true" />
      <el-table-column label="所属部门" align="center" prop="deptName" :show-overflow-tooltip="true" />
      <el-table-column label="状态" align="center" prop="status">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'danger'">
            {{ scope.row.status === '0' ? '正常' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="链上信息" align="center" width="120">
        <template slot-scope="scope">
          <el-button
            type="success"
            size="mini"
            @click="getChainInfo(scope.row)"
            :loading="scope.row.chainLoading"
          >获取
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 链上信息对话框 -->
    <el-dialog title="链上信息" :visible.sync="chainInfoDialogOpen" width="800px" append-to-body>
      <div v-if="chainInfo">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="描述信息">
            <div v-if="parsedDescription">
              <el-descriptions :column="2" size="small">
                <el-descriptions-item label="开始时间">{{ parsedDescription.startTime }}</el-descriptions-item>
                <el-descriptions-item label="结束时间">{{ parsedDescription.endTime }}</el-descriptions-item>
                <el-descriptions-item label="部门">{{ parsedDescription.department }}</el-descriptions-item>
                <el-descriptions-item label="检修线路">{{ parsedDescription.inspectionLine }}</el-descriptions-item>
                <el-descriptions-item label="是否异常">
                  <el-tag :type="parsedDescription.isAbnormal === '是' ? 'danger' : 'success'">
                    {{ parsedDescription.isAbnormal }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="设备是否损坏">
                  <el-tag :type="parsedDescription.hasEquipmentDamage === '是' ? 'danger' : 'success'">
                    {{ parsedDescription.hasEquipmentDamage }}
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
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="chainInfoDialogOpen = false">关 闭</el-button>
        <el-button type="primary" @click="copyChainInfo">复制信息</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listAllArchives } from "@/api/fisco/archive";
import { getRecord } from "@/api/fisco/record";

export default {
  name: "ArchiveSearch",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 总条数
      total: 0,
      // 档案表格数据
      archiveList: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10
      },
      // 链上信息对话框
      chainInfoDialogOpen: false,
      // 链上信息数据
      chainInfo: null,
      // 解析后的描述信息
      parsedDescription: null
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询档案列表 */
    getList() {
      this.loading = true;
      listAllArchives().then(response => {
        this.archiveList = response.data.map(item => ({
          ...item,
          chainLoading: false // 为每个记录添加加载状态
        }));
        this.total = this.archiveList.length;
        this.loading = false;
      }).catch(() => {
        this.loading = false;
      });
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.archiveId);
      this.single = selection.length !== 1;
      this.multiple = !selection.length;
    },
    /** 获取链上信息 */
    async getChainInfo(row) {
      try {
        // 设置当前行的加载状态
        this.$set(row, 'chainLoading', true);
        
        // 调用接口获取链上信息
        const response = await getRecord(row.archiveId);
        this.chainInfo = response.data;
        
        // 解析描述信息
        this.parseDescription();
        
        this.chainInfoDialogOpen = true;
        
        console.log("获取链上信息成功:", this.chainInfo);
      } catch (error) {
        console.error("获取链上信息失败:", error);
        this.$modal.msgError("获取链上信息失败：" + (error.message || "未知错误"));
      } finally {
        // 取消加载状态
        this.$set(row, 'chainLoading', false);
      }
    },
    /** 解析描述信息 */
    parseDescription() {
      try {
        if (this.chainInfo && this.chainInfo.description) {
          if (typeof this.chainInfo.description === 'string') {
            this.parsedDescription = JSON.parse(this.chainInfo.description);
          } else {
            this.parsedDescription = this.chainInfo.description;
          }
        } else {
          this.parsedDescription = null;
        }
      } catch (e) {
        console.warn("描述信息不是有效的JSON格式:", e);
        this.parsedDescription = null;
      }
    },
    /** 复制链上信息 */
    copyChainInfo() {
      let info = '';
      
      if (this.parsedDescription) {
        info = `描述信息:
开始时间: ${this.parsedDescription.startTime || ''}
结束时间: ${this.parsedDescription.endTime || ''}
部门: ${this.parsedDescription.department || ''}
检修线路: ${this.parsedDescription.inspectionLine || ''}
是否异常: ${this.parsedDescription.isAbnormal || ''}
设备是否损坏: ${this.parsedDescription.hasEquipmentDamage || ''}`;
      } else {
        info = `描述信息: ${this.chainInfo.description || ''}`;
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
    }
  }
};
</script>
