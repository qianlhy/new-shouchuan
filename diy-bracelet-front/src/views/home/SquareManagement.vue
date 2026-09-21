<template>
  <div class="xy-page square-management">
    <div class="xy-page-header">
      <h2 class="xy-page-title">灵感广场管理</h2>
    </div>

    <div class="xy-panel xy-filter-bar filters">
      <el-input
        v-model="filterTitle"
        placeholder="搜索标题"
        clearable
        style="width: 220px; margin-right: 10px;"
        @keyup.enter.native="handleSearch"
        @clear="handleSearch"
      />
      <el-select v-model="filterStatus" placeholder="展示状态" clearable @change="handleSearch" style="width: 140px; margin-right: 10px;">
        <el-option label="全部状态" :value="null" />
        <el-option label="展示中" :value="1" />
        <el-option label="已隐藏" :value="0" />
      </el-select>
      <el-select v-model="filterScope" placeholder="展示范围" clearable @change="handleSearch" style="width: 180px;">
        <el-option label="全部范围" :value="null" />
        <el-option label="仅灵感广场" :value="1" />
        <el-option label="仅推荐设计" :value="2" />
        <el-option label="灵感广场+推荐设计" :value="3" />
      </el-select>
      <el-button type="primary" style="margin-left: 10px;" @click="handleSearch">查询</el-button>
    </div>

    <div class="xy-panel xy-table-wrap">
      <el-table :data="list" style="width: 100%" v-loading="loading" :header-cell-style="{background: '#faf8ff', fontWeight: 'bold'}">
        <el-table-column type="index" label="序号" width="60" align="center" :index="indexMethod" />
        <el-table-column label="设计图" width="120" align="center">
          <template slot-scope="scope">
            <el-image
              v-if="scope.row.imageUrl"
              style="width: 88px; height: 88px; border-radius: 8px;"
              :src="getImageUrl(scope.row.imageUrl)"
              fit="cover"
              :preview-src-list="[getImageUrl(scope.row.imageUrl)]"
            />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="140" show-overflow-tooltip />
        <el-table-column prop="price" label="金额" width="90" align="center">
          <template slot-scope="scope">¥{{ Number(scope.row.price || 0).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="beadCount" label="珠子" width="70" align="center" />
        <el-table-column label="展示范围" width="160" align="center">
          <template slot-scope="scope">
            <el-tag size="small" type="info">{{ scopeLabel(scope.row.showScope) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" size="small">
              {{ scope.row.status === 1 ? '展示中' : '已隐藏' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="170" align="center" />
        <el-table-column label="操作" width="280" align="center" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" type="text" @click="openEdit(scope.row)">编辑</el-button>
            <el-button size="mini" type="text" @click="openScopeDialog(scope.row, 'hide')" v-if="scope.row.status === 1">
              隐藏
            </el-button>
            <el-button size="mini" type="text" @click="openScopeDialog(scope.row, 'show')" v-else>
              展示
            </el-button>
            <el-button size="mini" type="text" style="color:#F56C6C" @click="removeItem(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        style="margin-top: 12px; text-align: right;"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pagination.currentPage"
        :page-sizes="[20, 50, 100]"
        :page-size="pagination.pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="pagination.total"
      />
    </div>

    <el-dialog title="编辑广场作品" :visible.sync="dialogVisible" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="标题">
          <el-input v-model="form.title" maxlength="100" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" :max="9999" />
          <div class="tip">数值越大越靠前</div>
        </el-form-item>
        <el-form-item label="展示范围">
          <el-checkbox-group v-model="form.scopeChannels">
            <el-checkbox label="square">灵感广场</el-checkbox>
            <el-checkbox label="recommend">推荐设计</el-checkbox>
          </el-checkbox-group>
          <div class="tip">可多选，至少勾选一个</div>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">展示</el-radio>
            <el-radio :label="0">隐藏</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" class="xy-btn-gradient" :loading="saving" @click="saveEdit">保存</el-button>
      </div>
    </el-dialog>

    <el-dialog :title="scopeDialogTitle" :visible.sync="scopeDialogVisible" width="420px">
      <div class="scope-tip">{{ scopeDialogHint }}</div>
      <el-checkbox-group v-model="scopeDialogChannels" class="scope-checks">
        <el-checkbox label="square">灵感广场</el-checkbox>
        <el-checkbox label="recommend">推荐设计</el-checkbox>
      </el-checkbox-group>
      <div slot="footer">
        <el-button @click="scopeDialogVisible = false">取消</el-button>
        <el-button type="primary" class="xy-btn-gradient" :loading="scopeSaving" @click="confirmScopeDialog">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  updateSquareItem,
  deleteSquareItem,
  getSquarePage
} from '@/api/admin'

export default {
  name: 'SquareManagement',
  data () {
    return {
      loading: false,
      saving: false,
      list: [],
      filterTitle: '',
      filterStatus: null,
      filterScope: null,
      pagination: {
        currentPage: 1,
        pageSize: 20,
        total: 0
      },
      dialogVisible: false,
      form: {
        id: null,
        title: '',
        sort: 0,
        status: 0,
        scopeChannels: ['square', 'recommend']
      },
      scopeDialogVisible: false,
      scopeDialogMode: 'hide',
      scopeDialogRow: null,
      scopeDialogChannels: [],
      scopeSaving: false
    }
  },
  computed: {
    scopeDialogTitle () {
      return this.scopeDialogMode === 'hide' ? '隐藏哪个' : '展示到哪里'
    },
    scopeDialogHint () {
      return this.scopeDialogMode === 'hide'
        ? '勾选要从哪些入口隐藏该作品（可多选）'
        : '勾选要在哪些入口展示该作品（可多选）'
    }
  },
  created () {
    this.fetchList()
  },
  methods: {
    indexMethod (index) {
      return (this.pagination.currentPage - 1) * this.pagination.pageSize + index + 1
    },
    scopeLabel (scope) {
      if (scope === 1) return '仅灵感广场'
      if (scope === 2) return '仅推荐设计'
      return '灵感广场+推荐设计'
    },
    scopeToChannels (scope) {
      if (scope === 1) return ['square']
      if (scope === 2) return ['recommend']
      return ['square', 'recommend']
    },
    channelsToScope (channels) {
      const hasSquare = channels.indexOf('square') >= 0
      const hasRecommend = channels.indexOf('recommend') >= 0
      if (hasSquare && hasRecommend) return 3
      if (hasSquare) return 1
      if (hasRecommend) return 2
      return null
    },
    visibleChannels (row) {
      if (!row || row.status !== 1) return []
      return this.scopeToChannels(row.showScope != null ? row.showScope : 3)
    },
    getImageUrl (relativePath) {
      if (!relativePath) return ''
      if (/^https?:\/\//i.test(relativePath)) return relativePath
      const base = (process.env.VUE_APP_API_TARGET || process.env.VUE_APP_BASE_URL || window.location.origin || '').replace(/\/$/, '')
      return relativePath.startsWith('/') ? `${base}${relativePath}` : `${base}/${relativePath}`
    },
    async fetchList () {
      this.loading = true
      try {
        const params = {
          page: this.pagination.currentPage,
          pageSize: this.pagination.pageSize
        }
        if (this.filterStatus === 0 || this.filterStatus === 1) {
          params.status = this.filterStatus
        }
        if (this.filterScope === 1 || this.filterScope === 2 || this.filterScope === 3) {
          params.showScope = this.filterScope
        }
        if (this.filterTitle) {
          params.title = this.filterTitle
        }
        const res = await getSquarePage(params)
        const data = res.data || {}
        this.list = data.records || data.list || []
        this.pagination.total = Number(data.total || 0)
      } catch (e) {
        this.$message.error(e.message || '加载失败')
        this.list = []
      } finally {
        this.loading = false
      }
    },
    handleSearch () {
      this.pagination.currentPage = 1
      this.fetchList()
    },
    handleSizeChange (size) {
      this.pagination.pageSize = size
      this.pagination.currentPage = 1
      this.fetchList()
    },
    handleCurrentChange (page) {
      this.pagination.currentPage = page
      this.fetchList()
    },
    openScopeDialog (row, mode) {
      this.scopeDialogMode = mode
      this.scopeDialogRow = row
      if (mode === 'hide') {
        this.scopeDialogChannels = this.visibleChannels(row).slice()
      } else {
        const prev = row.showScope != null ? row.showScope : 3
        this.scopeDialogChannels = this.scopeToChannels(prev)
      }
      this.scopeDialogVisible = true
    },
    async confirmScopeDialog () {
      const row = this.scopeDialogRow
      if (!row) return
      const selected = this.scopeDialogChannels || []
      if (!selected.length) {
        this.$message.warning(this.scopeDialogMode === 'hide' ? '请勾选要隐藏的入口' : '请勾选要展示的入口')
        return
      }

      let nextStatus
      let nextScope

      if (this.scopeDialogMode === 'hide') {
        const current = this.visibleChannels(row)
        const remain = current.filter(c => selected.indexOf(c) < 0)
        if (!remain.length) {
          nextStatus = 0
          nextScope = this.channelsToScope(selected) || (row.showScope != null ? row.showScope : 3)
        } else {
          nextStatus = 1
          nextScope = this.channelsToScope(remain)
        }
      } else {
        nextStatus = 1
        nextScope = this.channelsToScope(selected)
      }

      this.scopeSaving = true
      try {
        await updateSquareItem({
          id: row.id,
          title: row.title,
          sort: row.sort != null ? row.sort : 0,
          status: nextStatus,
          showScope: nextScope
        })
        this.$message.success(this.scopeDialogMode === 'hide' ? '已更新隐藏范围' : '已更新展示范围')
        this.scopeDialogVisible = false
        this.fetchList()
      } catch (e) {
        this.$message.error(e.message || '操作失败')
      } finally {
        this.scopeSaving = false
      }
    },
    openEdit (row) {
      this.form = {
        id: row.id,
        title: row.title || '',
        sort: row.sort != null ? row.sort : 0,
        status: row.status != null ? row.status : 0,
        scopeChannels: this.scopeToChannels(row.showScope != null ? row.showScope : 3)
      }
      this.dialogVisible = true
    },
    async saveEdit () {
      const showScope = this.channelsToScope(this.form.scopeChannels || [])
      if (!showScope) {
        this.$message.warning('请至少勾选一个展示入口')
        return
      }
      this.saving = true
      try {
        await updateSquareItem({
          id: this.form.id,
          title: this.form.title,
          sort: this.form.sort,
          status: this.form.status,
          showScope
        })
        this.$message.success('已保存')
        this.dialogVisible = false
        this.fetchList()
      } catch (e) {
        this.$message.error(e.message || '保存失败')
      } finally {
        this.saving = false
      }
    },
    async removeItem (row) {
      try {
        await this.$confirm('删除后不可恢复，确定删除？', '提示', { type: 'warning' })
        await deleteSquareItem(row.id)
        this.$message.success('已删除')
        this.fetchList()
      } catch (e) {
        if (e !== 'cancel') this.$message.error(e.message || '删除失败')
      }
    }
  }
}
</script>

<style scoped>
.filters {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  margin-bottom: 16px;
}
.tip {
  margin-top: 6px;
  font-size: 12px;
  color: #909399;
}
.scope-tip {
  margin-bottom: 16px;
  color: #606266;
  font-size: 14px;
}
.scope-checks {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
</style>
