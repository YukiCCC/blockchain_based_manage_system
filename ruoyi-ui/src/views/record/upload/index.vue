<template>
  <div class="app-container">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>添加区块链记录</span>
      </div>
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="记录ID" prop="recordId">
          <el-input v-model="form.recordId" placeholder="请输入记录ID" />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            placeholder="选择开始时间"
            value-format="yyyy-MM-dd HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
            v-model="form.endTime"
            type="datetime"
            placeholder="选择结束时间"
            value-format="yyyy-MM-dd HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="部门" prop="department">
          <el-input v-model="form.department" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="查勘数据" prop="inspectionLine">
          <el-input v-model="form.inspectionLine" placeholder="请上传查勘数据" />
        </el-form-item>
        <el-form-item label="是否异常" prop="isAbnormal">
          <el-select v-model="form.isAbnormal" placeholder="请选择是否异常" style="width: 100%">
            <el-option label="是" value="是" />
            <el-option label="否" value="否" />
          </el-select>
        </el-form-item>
        <el-form-item label="数据类型" prop="hasEquipmentDamage">
          <el-select v-model="form.hasEquipmentDamage" placeholder="请选择设备是否损坏" style="width: 100%" disabled>
            <el-option label="无人机命令集" value="是" />
            <el-option label="无人机查勘数据" value="否" />
          </el-select>
        </el-form-item>
        <el-form-item label="任务ID" prop="remark">
          <el-input v-model="form.remark" placeholder="请输入任务ID，建议使用UUID格式" />
          <el-button type="primary" size="mini" style="margin-top: 5px" @click="generateUUID">生成UUID</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="loading">提交</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- JSON预览对话框 -->
    <el-dialog title="JSON预览" :visible.sync="jsonPreviewOpen" width="600px" append-to-body>
      <pre style="background-color: #f5f5f5; padding: 15px; border-radius: 5px; max-height: 400px; overflow: auto;">{{ jsonPreview }}</pre>
      <div slot="footer" class="dialog-footer">
        <el-button @click="jsonPreviewOpen = false">关 闭</el-button>
        <el-button type="primary" @click="copyJsonToClipboard">复制</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { addRecord } from "@/api/fisco/record";
// import { addArchive } from "@/api/fisco/archive";
import { getUserProfile } from "@/api/system/user";

export default {
  name: "FiscoRecord",
  data() {
    return {
      // 遮罩层
      loading: false,
      // 是否显示JSON预览弹出层
      jsonPreviewOpen: false,
      // 当前用户信息
      userInfo: {},
      // 表单参数
      form: {
        recordId: undefined,
        startTime: undefined,
        endTime: undefined,
        department: undefined,
        inspectionLine: undefined,
        isAbnormal: "否",
        hasEquipmentDamage: "否",
        remark: undefined
      },
      // JSON预览内容
      jsonPreview: "",
      // 表单校验
      rules: {
        recordId: [
          { required: true, message: "记录ID不能为空", trigger: "blur" }
        ],
        startTime: [
          { required: true, message: "开始时间不能为空", trigger: "blur" }
        ],
        endTime: [
          { required: true, message: "结束时间不能为空", trigger: "blur" }
        ],
        department: [
          { required: true, message: "部门不能为空", trigger: "blur" }
        ],
        inspectionLine: [
          { required: true, message: "采集数据不能为空", trigger: "blur" }
        ],
        isAbnormal: [
          { required: true, message: "请选择是否异常", trigger: "change" }
        ],
        hasEquipmentDamage: [
          { required: true, message: "请选择设备是否损坏", trigger: "change" }
        ],
        remark: [
          { required: true, message: "任务ID不能为空", trigger: "blur" }
        ]
      }
    };
  },
  created() {
    this.getUserInfo();
  },
  methods: {
    // 获取用户信息
    async getUserInfo() {
      try {
        const response = await getUserProfile();
        this.userInfo = response.data;
        console.log("获取用户信息成功:", this.userInfo);
        
        // 自动填充部门信息到表单
        if (this.userInfo.dept && this.userInfo.dept.deptName) {
          this.form.department = this.userInfo.dept.deptName;
        }
      } catch (error) {
        console.error("获取用户信息失败:", error);
        this.$modal.msgError("获取用户信息失败");
      }
    },
    // 表单重置
    reset() {
      this.form = {
        recordId: undefined,
        startTime: undefined,
        endTime: undefined,
        department: undefined,
        inspectionLine: undefined,
        isAbnormal: "否",
        hasEquipmentDamage: "否",
        remark: undefined
      };
      this.resetForm("form");
    },
    /** 提交按钮 */
    async submitForm() {
      this.$refs["form"].validate(async (valid) => {
        if (valid) {
          this.loading = true;
          try {
            // 1. 先添加区块链记录
            const descObj = {
              startTime: this.form.startTime,
              endTime: this.form.endTime,
              department: this.form.department,
              inspectionLine: this.form.inspectionLine,
              isAbnormal: this.form.isAbnormal,
              hasEquipmentDamage: this.form.hasEquipmentDamage
            };
            const description = JSON.stringify(descObj);

            const recordResponse = await addRecord(this.form.recordId, description, this.form.remark);
            console.log("添加区块链记录成功:", recordResponse);

            // 2. 添加档案记录 (由于archive.js被删除，暂时注释)
            /*
            const archiveData = {
              recordId: parseInt(this.form.recordId), // 将记录ID作为recordId传入
              name: this.userInfo.nickName || this.userInfo.userName || "未知用户",
              employeeId: this.userInfo.userName || "未知工号",
              deptId: this.userInfo.deptId || null,
              deptName: this.userInfo.dept?.deptName || "未知部门"
            };

            console.log("准备添加档案数据:", archiveData);
            const archiveResponse = await addArchive(archiveData);
            console.log("添加档案记录成功:", archiveResponse);
            */

            this.$modal.msgSuccess("添加成功");
            this.reset();
          } catch (error) {
            console.error("添加失败:", error);
            this.$modal.msgError("添加失败：" + (error.message || "未知错误"));
          } finally {
            this.loading = false;
          }
        }
      });
    },
    // 预览描述信息JSON
    previewDescription() {
      const descObj = {
        startTime: this.form.startTime,
        endTime: this.form.endTime,
        department: this.form.department,
        inspectionLine: this.form.inspectionLine,
        isAbnormal: this.form.isAbnormal,
        hasEquipmentDamage: this.form.hasEquipmentDamage
      };
      this.jsonPreview = JSON.stringify(descObj, null, 2);
      this.jsonPreviewOpen = true;
    },
    // 复制JSON到剪贴板
    copyJsonToClipboard() {
      const textarea = document.createElement('textarea');
      textarea.value = this.jsonPreview;
      document.body.appendChild(textarea);
      textarea.select();
      document.execCommand('copy');
      document.body.removeChild(textarea);
      this.$message.success('复制成功');
    },
    // 生成UUID
    generateUUID() {
      this.form.remark = this.createUUID();
    },
    // 创建UUID
    createUUID() {
      return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
        const r = Math.random() * 16 | 0;
        const v = c === 'x' ? r : (r & 0x3 | 0x8);
        return v.toString(16);
      });
    }
  }
};
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.box-card {
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
}
</style>