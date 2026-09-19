<template>
  <div class="xy-page product-management">
    <div class="xy-page-header">
      <h2 class="xy-page-title">商品管理</h2>
      <el-button type="primary" class="xy-btn-gradient" icon="el-icon-plus" @click="addProduct">添加商品</el-button>
    </div>

    <div class="xy-panel xy-filter-bar">
      <el-form :inline="true" :model="filterForm" class="demo-form-inline">
        <el-form-item label="分类">
          <el-select v-model="filterForm.categoryId" placeholder="请选择分类" clearable>
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.name"
              :value="category.id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="请选择状态" clearable>
            <el-option label="上架" :value="1"></el-option>
            <el-option label="下架" :value="0"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="商品名称">
          <el-input v-model="filterForm.title" placeholder="请输入商品名称" clearable></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchProducts">查询</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="xy-panel xy-table-wrap">
    <el-table :data="products" style="width: 100%" v-loading="loading">
      <el-table-column type="index" label="序号" width="80"></el-table-column>
      <el-table-column label="封面图" width="100">
        <template slot-scope="scope">
          <el-image
            style="width: 60px; height: 60px"
            :src="getImageUrl(scope.row.coverImage)"
            fit="cover"
            :preview-src-list="[getImageUrl(scope.row.coverImage)]">
          </el-image>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="商品名称"></el-table-column>
      <el-table-column prop="categoryName" label="分类"></el-table-column>
      <el-table-column prop="price" label="价格" width="100">
        <template slot-scope="scope">
          ¥{{ scope.row.price }}
        </template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="80"></el-table-column>
      <el-table-column label="DIY模板" width="100">
        <template slot-scope="scope">
          <el-tag :type="scope.row.hasDiyTemplate ? 'success' : 'info'" size="mini">
            {{ scope.row.hasDiyTemplate ? '已配置' : '普通' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '上架' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template slot-scope="scope">
          <el-button size="mini" type="text" @click="viewProductDetail(scope.row.id)">查看</el-button>
          <el-button size="mini" type="text" @click="editProduct(scope.row)">编辑</el-button>
          <el-button size="mini" type="text" @click="deleteProduct(scope.row.id)">删除</el-button>
          <el-button size="mini" type="text" @click="toggleStatus(scope.row)">
            {{ scope.row.status === 1 ? '下架' : '上架' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页控件 -->
    <el-pagination
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      :current-page="pagination.currentPage"
      :page-sizes="[10, 20, 50, 100]"
      :page-size="pagination.pageSize"
      layout="total, sizes, prev, pager, next, jumper"
      :total="pagination.total"
      style="margin-top: 20px; text-align: right;">
    </el-pagination>
    </div>

    <!-- 添加/编辑商品对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="760px" top="4vh">
      <el-form :model="productForm" :rules="rules" ref="productForm" label-width="110px">
        <el-form-item label="商品名称" prop="title">
          <el-input v-model="productForm.title" placeholder="请输入商品名称"></el-input>
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="productForm.categoryId" placeholder="请选择分类">
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.name"
              :value="category.id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="封面图" prop="coverImage">
          <el-upload
            class="avatar-uploader"
            :action="uploadAction"
            :data="{module: 'product'}"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
            :headers="{authentication: $store?.state?.token || localStorage.getItem('token')}"
            name="file">
            <img v-if="productForm.coverImage" :src="getImageUrl(productForm.coverImage)" class="avatar">
            <i v-else class="el-icon-plus avatar-uploader-icon"></i>
          </el-upload>
        </el-form-item>
        <el-form-item label="详情图">
          <el-upload
            class="detail-uploader"
            :action="uploadAction"
            :data="{module: 'product'}"
            list-type="picture-card"
            :file-list="detailFileList"
            :on-success="handleDetailSuccess"
            :on-remove="handleDetailRemove"
            :headers="{authentication: $store?.state?.token || localStorage.getItem('token')}"
            multiple
            name="file">
            <i class="el-icon-plus"></i>
          </el-upload>
          <el-button type="text" @click="clearDetailImages">清空详情图</el-button>
        </el-form-item>
        <el-form-item label="商品描述" prop="description">
          <el-input
            type="textarea"
            v-model="productForm.description"
            placeholder="请输入商品描述"
            :rows="3">
          </el-input>
        </el-form-item>
        <el-form-item label="创作者">
          <el-input v-model="productForm.creatorName" placeholder="如：祈愿手作（详情页展示）" maxlength="32"></el-input>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="productForm.price" :min="0" :precision="2" :step="0.1"></el-input-number>
        </el-form-item>
        <el-form-item label="重量(g)" prop="weight">
          <el-input-number v-model="productForm.weight" :min="0" :precision="1"></el-input-number>
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="productForm.stock" :min="0"></el-input-number>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch
            v-model="productForm.status"
            :active-value="1"
            :inactive-value="0"
            active-text="上架"
            inactive-text="下架">
          </el-switch>
        </el-form-item>

        <el-divider content-position="left">DIY 设计模板</el-divider>
        <el-form-item label="启用模板">
          <el-switch v-model="diyEnabled" active-text="详情可带入制作台" inactive-text="普通成品"></el-switch>
        </el-form-item>
        <template v-if="diyEnabled">
          <el-form-item label="手围(cm)">
            <el-input-number v-model="diyHandSize" :min="10" :max="24" :step="0.5" :precision="1"></el-input-number>
          </el-form-item>
          <el-form-item label="搭配清单">
            <div class="diy-rows">
              <div v-for="(row, idx) in diyRows" :key="idx" class="diy-row">
                <el-select
                  v-model="row.materialId"
                  filterable
                  remote
                  clearable
                  placeholder="搜索 DIY 材料"
                  :remote-method="searchDiyMaterials"
                  :loading="diyMaterialLoading"
                  style="width: 260px"
                  @focus="searchDiyMaterials('')"
                  @change="(id) => onDiyMaterialPicked(row, id)">
                  <el-option
                    v-for="m in diyMaterialOptions"
                    :key="m.id"
                    :label="`${m.title}（${m.size || '-'}mm / ¥${m.price}）`"
                    :value="m.id">
                  </el-option>
                </el-select>
                <el-input-number v-model="row.qty" :min="1" :max="99" size="small" style="width: 110px; margin: 0 8px"></el-input-number>
                <span class="diy-row-meta" v-if="row.title">{{ row.size || '-' }}mm · ¥{{ row.price }}/颗</span>
                <el-button type="text" style="color:#F56C6C" @click="removeDiyRow(idx)">删除</el-button>
              </div>
              <el-button type="primary" plain size="mini" icon="el-icon-plus" @click="addDiyRow">添加材料</el-button>
              <div class="diy-hint">保存后小程序详情页会展示「原设计图 / 搭配清单」，并可一键带入制作台。</div>
            </div>
          </el-form-item>
        </template>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="saveProduct">确 定</el-button>
      </div>
    </el-dialog>

    <!-- 商品详情对话框 -->
    <el-dialog title="商品详情" :visible.sync="detailDialogVisible" width="600px">
      <el-form label-width="100px">
        <el-form-item label="商品名称">
          <span>{{ productDetail.title }}</span>
        </el-form-item>
        <el-form-item label="分类">
          <span>{{ productDetail.categoryName }}</span>
        </el-form-item>
        <el-form-item label="封面图">
          <el-image
            style="width: 100px; height: 100px"
            :src="getImageUrl(productDetail.coverImage)"
            fit="cover">
          </el-image>
        </el-form-item>
        <el-form-item label="商品描述">
          <span>{{ productDetail.description }}</span>
        </el-form-item>
        <el-form-item label="创作者">
          <span>{{ productDetail.creatorName || '—' }}</span>
        </el-form-item>
        <el-form-item label="DIY模板">
          <el-tag :type="productDetail.diyData ? 'success' : 'info'">
            {{ productDetail.diyData ? '已配置' : '未配置' }}
          </el-tag>
        </el-form-item>
        <el-form-item label="价格">
          <span>¥{{ productDetail.price }}</span>
        </el-form-item>
        <el-form-item label="重量(g)">
          <span>{{ productDetail.weight }}</span>
        </el-form-item>
        <el-form-item label="库存">
          <span>{{ productDetail.stock }}</span>
        </el-form-item>
        <el-form-item label="状态">
          <el-tag :type="productDetail.status === 1 ? 'success' : 'danger'">
            {{ productDetail.status === 1 ? '上架' : '下架' }}
          </el-tag>
        </el-form-item>
        <el-form-item label="详情图" v-if="productDetailImages.length">
          <div class="detail-images">
            <el-image
              v-for="(img, idx) in productDetailImages"
              :key="idx"
              :src="getImageUrl(img)"
              style="width: 80px; height: 80px; margin-right: 8px"
              :preview-src-list="productDetailPreviewList"
              fit="cover">
            </el-image>
          </div>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getProductList, addProduct, updateProduct, deleteProduct, getAllCategories, getProductDetail, getDiyMaterialList } from '@/api/admin'

export default {
  name: 'ProductManagement',
  data () {
    return {
      loading: false,
      products: [],
      categories: [],
      filterForm: {
        categoryId: null,
        status: null,
        title: null
      },
      pagination: {
        currentPage: 1,
        pageSize: 20,
        total: 0
      },
      dialogVisible: false,
      dialogTitle: '添加商品',
      detailDialogVisible: false,
      productDetail: {},
      productForm: {
        id: null,
        categoryId: null,
        title: '',
        description: '',
        creatorName: '',
        coverImage: '',
        images: [],
        price: 0,
        weight: 0,
        stock: 0,
        status: 1
      },
      diyEnabled: false,
      diyHandSize: 16,
      diyRows: [],
      diyMaterialOptions: [],
      diyMaterialLoading: false,
      imagesTouched: false,
      originalImages: null,
      rules: {
        title: [
          { required: true, message: '请输入商品名称', trigger: 'blur' }
        ],
        categoryId: [
          { required: true, message: '请选择分类', trigger: 'change' }
        ],
        coverImage: [
          { required: true, message: '请上传封面图', trigger: 'change' }
        ],
        price: [
          { required: true, message: '请输入价格', trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    uploadAction () {
      if (process.env.NODE_ENV === 'development') {
        return '/admin/common/upload'
      }
      const target = (process.env.VUE_APP_API_TARGET || process.env.VUE_APP_BASE_URL || window.location.origin || '').replace(/\/$/, '')
      return `${target}/admin/common/upload`
    },
    detailFileList () {
      const list = (this.productForm.images || []).map((rel, idx) => ({
        name: `image-${idx}`,
        url: this.getImageUrl(rel),
        response: { data: rel }
      }))
      return list
    },
    productDetailImages () {
      return this.normalizeImages(this.productDetail?.images)
    },
    productDetailPreviewList () {
      return this.productDetailImages.map(img => this.getImageUrl(img))
    }
  },
  created () {
    this.fetchCategories()
    this.fetchProducts()
  },
  methods: {
    async fetchCategories () {
      try {
        const res = await getAllCategories()
        this.categories = res.data || []
      } catch (error) {
        console.error('获取分类列表失败:', error)
        this.$message.error('获取分类列表失败')
      }
    },
    async fetchProducts () {
      this.loading = true
      try {
        const params = {
          categoryId: this.filterForm.categoryId,
          status: this.filterForm.status,
          title: this.filterForm.title,
          page: this.pagination.currentPage,
          pageSize: this.pagination.pageSize
        }
        // 移除空值参数
        Object.keys(params).forEach(key => {
          if (params[key] === null || params[key] === undefined) {
            delete params[key]
          }
        })

        const res = await getProductList(params)
        // 直接使用后端返回的商品数据，其中已包含分类名称
        // 处理图片路径
        this.products = (res.data.products || []).map(product => ({
          ...product,
          coverImage: this.getImageUrl(product.coverImage)
        }))

        // 设置分页信息
        this.pagination.total = res.data.total || 0
        this.pagination.currentPage = res.data.page || 1
        this.pagination.pageSize = res.data.size || this.pagination.pageSize
      } catch (error) {
        console.error('获取商品列表失败:', error)
        this.$message.error('获取商品列表失败')
      } finally {
        this.loading = false
      }
    },
    handleSizeChange (val) {
      this.pagination.pageSize = val
      this.pagination.currentPage = 1
      this.fetchProducts()
    },
    handleCurrentChange (val) {
      this.pagination.currentPage = val
      this.fetchProducts()
    },
    addProduct () {
      this.dialogTitle = '添加商品'
      this.productForm = {
        id: null,
        categoryId: null,
        title: '',
        description: '',
        creatorName: '',
        coverImage: '',
        images: [],
        price: 0,
        weight: 0,
        stock: 0,
        status: 1
      }
      this.diyEnabled = false
      this.diyHandSize = 16
      this.diyRows = []
      this.diyMaterialOptions = []
      this.imagesTouched = false
      this.originalImages = null
      this.dialogVisible = true
    },
    async editProduct (product) {
      this.dialogTitle = '编辑商品'
      try {
        const res = await getProductDetail(product.id)
        const p = res.data.product || {}
        this.productForm = {
          id: p.id,
          categoryId: p.categoryId,
          title: p.title || '',
          description: p.description || '',
          creatorName: p.creatorName || '',
          coverImage: p.coverImage || '',
          images: Array.isArray(p.detailImages)
            ? p.detailImages
            : (Array.isArray(p.images) ? p.images : []),
          price: p.price || 0,
          weight: p.weight || 0,
          stock: p.stock || 0,
          status: p.status !== undefined ? p.status : 1
        }
        this.applyDiyDataToForm(p.diyData)
        this.imagesTouched = false
        this.originalImages = [...this.productForm.images]
        this.dialogVisible = true
      } catch (error) {
        console.error('获取商品详情失败:', error)
        this.$message.error('获取商品详情失败')
      }
    },
    async saveProduct () {
      this.$refs.productForm.validate(async (valid) => {
        if (valid) {
          try {
            if (this.diyEnabled && (!this.diyRows.length || this.diyRows.some(r => !r.materialId))) {
              this.$message.warning('请完善 DIY 搭配清单中的材料')
              return
            }
            const diyData = this.diyEnabled ? this.buildDiyData() : ''
            if (this.productForm.id) {
              // 更新商品
              const payload = {
                id: this.productForm.id,
                categoryId: this.productForm.categoryId,
                title: this.productForm.title,
                description: this.productForm.description,
                creatorName: this.productForm.creatorName || '',
                coverImage: this.productForm.coverImage,
                // 空串也会写入库，用于关闭 DIY 模板
                diyData: diyData == null ? '' : diyData,
                price: this.productForm.price,
                weight: this.productForm.weight,
                stock: this.productForm.stock,
                status: this.productForm.status
              }
              if (this.imagesTouched) {
                payload.images = this.productForm.images
              }
              await updateProduct(payload)
              this.$message.success('更新成功')
            } else {
              // 添加商品
              const payload = {
                ...this.productForm,
                creatorName: this.productForm.creatorName || '',
                diyData
              }
              await addProduct(payload)
              this.$message.success('添加成功')
            }
            this.dialogVisible = false
            this.fetchProducts()
          } catch (error) {
            console.error('保存商品失败:', error)
            // 显示后端返回的错误信息
            const errorMsg = error.response?.data?.msg || error.message || '保存商品失败'
            this.$message.error(errorMsg)
          }
        }
      })
    },
    addDiyRow () {
      this.diyRows.push({
        materialId: null,
        qty: 1,
        title: '',
        size: null,
        price: 0,
        color: '#e8e8e8',
        imageUrl: ''
      })
    },
    removeDiyRow (idx) {
      this.diyRows.splice(idx, 1)
    },
    async searchDiyMaterials (query) {
      this.diyMaterialLoading = true
      try {
        const q = (query || '').trim()
        const res = await getDiyMaterialList({
          title: q || undefined,
          limit: 80
        })
        const list = res.data || []
        this.diyMaterialOptions = Array.isArray(list)
          ? list.filter(m => m.status === undefined || m.status === 1)
          : []
      } catch (e) {
        console.error('搜索DIY材料失败', e)
        this.diyMaterialOptions = []
      } finally {
        this.diyMaterialLoading = false
      }
    },
    onDiyMaterialPicked (row, id) {
      const m = this.diyMaterialOptions.find(it => it.id === id)
      if (!m) return
      row.materialId = m.id
      row.title = m.title || ''
      row.size = m.size
      row.price = m.price
      row.color = m.color || '#e8e8e8'
      row.imageUrl = m.imageUrl || ''
    },
    applyDiyDataToForm (raw) {
      this.diyEnabled = false
      this.diyHandSize = 16
      this.diyRows = []
      this.diyMaterialOptions = []
      if (!raw) return
      try {
        const diy = typeof raw === 'string' ? JSON.parse(raw) : raw
        if (!diy || !Array.isArray(diy.beads) || !diy.beads.length) return
        this.diyEnabled = true
        this.diyHandSize = diy.size != null ? Number(diy.size) : 16
        const map = {}
        diy.beads.forEach(b => {
          const id = b.productId
          if (id == null) return
          if (!map[id]) {
            map[id] = {
              materialId: id,
              qty: 0,
              title: b.title || b.name || '',
              size: b.size,
              price: b.price,
              color: b.color || '#e8e8e8',
              imageUrl: b.imageUrl || ''
            }
          }
          map[id].qty += 1
        })
        this.diyRows = Object.values(map)
        this.diyMaterialOptions = this.diyRows.map(r => ({
          id: r.materialId,
          title: r.title,
          size: r.size,
          price: r.price,
          color: r.color,
          imageUrl: r.imageUrl
        }))
      } catch (e) {
        console.error('解析 diyData 失败', e)
      }
    },
    buildDiyData () {
      const beads = []
      let pos = 1
      this.diyRows.forEach(row => {
        const qty = Math.max(1, Number(row.qty) || 1)
        for (let i = 0; i < qty; i++) {
          beads.push({
            position: pos++,
            productId: row.materialId,
            title: row.title,
            name: row.title,
            price: row.price,
            size: row.size,
            color: row.color || '#e8e8e8',
            imageUrl: row.imageUrl
          })
        }
      })
      return JSON.stringify({
        title: this.productForm.title,
        price: this.productForm.price,
        imageUrl: this.productForm.coverImage,
        color: beads[0]?.color || '#e8e8e8',
        size: this.diyHandSize,
        beadCount: beads.length,
        description: this.productForm.description || '',
        beads
      })
    },
    async deleteProduct (id) {
      this.$confirm('确定要删除这个商品吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await deleteProduct(id)
          this.$message.success('删除成功')
          this.fetchProducts()
        } catch (error) {
          console.error('删除商品失败:', error)
          // 显示后端返回的错误信息
          const errorMsg = error.response?.data?.msg || error.message || '删除商品失败'
          this.$message.error(errorMsg)
        }
      }).catch(() => {
        // 用户取消删除操作
      })
    },
    async toggleStatus (product) {
      try {
        const updatedProduct = {
          ...product,
          status: product.status === 1 ? 0 : 1
        }
        await updateProduct(updatedProduct)
        this.$message.success('状态更新成功')
        this.fetchProducts()
      } catch (error) {
        console.error('更新状态失败:', error)
        this.$message.error('更新状态失败')
      }
    },
    async viewProductDetail (id) {
      try {
        const res = await getProductDetail(id)
        const product = res.data.product || {}
        // 处理图片路径
        this.productDetail = {
          ...product,
          coverImage: this.getImageUrl(product.coverImage),
          images: this.normalizeImages(product.detailImages || product.images)
        }
        this.detailDialogVisible = true
      } catch (error) {
        console.error('获取商品详情失败:', error)
        this.$message.error('获取商品详情失败')
      }
    },
    handleAvatarSuccess (response, file, fileList) {
      if (response.code === 1) {
        this.productForm.coverImage = response.data
        this.$refs.productForm.validateField('coverImage')
        this.$message.success('图片上传成功')
      } else {
        this.$message.error('图片上传失败: ' + (response.msg || '未知错误'))
      }
    },
    handleDetailSuccess (response, file, fileList) {
      if (response.code === 1) {
        this.productForm.images = [...(this.productForm.images || []), response.data]
        this.imagesTouched = true
        this.$message.success('详情图上传成功')
      } else {
        this.$message.error('详情图上传失败: ' + (response.msg || '未知错误'))
      }
    },
    handleDetailRemove (file, fileList) {
      // 优先使用上传返回的相对路径
      const rel = file?.response?.data || this.toRelativePath(file?.url)
      if (rel) {
        this.productForm.images = (this.productForm.images || []).filter(it => it !== rel)
        this.imagesTouched = true
      }
    },
    clearDetailImages () {
      this.productForm.images = []
      this.imagesTouched = true
    },
    getImageUrl (relativePath) {
      if (!relativePath) return ''
      if (relativePath.startsWith('http://') || relativePath.startsWith('https://')) return relativePath
      const base = (process.env.VUE_APP_API_TARGET || process.env.VUE_APP_BASE_URL || window.location.origin || '').replace(/\/$/, '')
      if (relativePath.startsWith('/')) {
        return `${base}${relativePath}`
      }
      return `${base}/${relativePath}`
    },
    toRelativePath (url) {
      if (!url) return ''
      const base = (process.env.VUE_APP_API_TARGET || process.env.VUE_APP_BASE_URL || window.location.origin || '').replace(/\/$/, '')
      if (url.startsWith(base)) {
        const rel = url.substring(base.length)
        return rel.startsWith('/') ? rel : `/${rel}`
      }
      return url
    },
    normalizeImages (images) {
      if (!images) return []
      if (Array.isArray(images)) return images
      if (typeof images === 'string') {
        const trimmed = images.trim()
        if (!trimmed) return []
        // 兼容后端可能返回 JSON 字符串
        if (trimmed.startsWith('[')) {
          try {
            const parsed = JSON.parse(trimmed)
            return Array.isArray(parsed) ? parsed : []
          } catch (e) {
            return []
          }
        }
        // 兼容逗号分隔
        if (trimmed.includes(',')) {
          return trimmed.split(',').map(s => s.trim()).filter(Boolean)
        }
        return [trimmed]
      }
      return []
    },
    beforeAvatarUpload (file) {
      const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
      const isLt2M = file.size / 1024 / 1024 < 2

      if (!isJPG) {
        this.$message.error('上传头像图片只能是 JPG/PNG 格式!')
      }
      if (!isLt2M) {
        this.$message.error('上传头像图片大小不能超过 2MB!')
      }
      return isJPG && isLt2M
    }
  }
}
</script>

<style scoped>
.product-management {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.el-table {
  flex: 1;
  overflow-y: auto;
}

.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.avatar-uploader .el-upload:hover {
  border-color: #8B5CF6;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  line-height: 100px;
  text-align: center;
}

.avatar {
  width: 100px;
  height: 100px;
  display: block;
}

.detail-images {
  display: flex;
  flex-wrap: wrap;
}

.detail-images ::v-deep .el-image {
  margin-bottom: 8px;
}
.detail-uploader ::v-deep .el-upload-list__item-thumbnail {
  object-fit: cover;
}
.diy-rows {
  width: 100%;
}
.diy-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  margin-bottom: 10px;
}
.diy-row-meta {
  color: #909399;
  font-size: 12px;
  margin-right: 8px;
}
.diy-hint {
  margin-top: 8px;
  color: #909399;
  font-size: 12px;
  line-height: 1.5;
}
</style>
