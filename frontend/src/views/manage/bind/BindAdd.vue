<template>
  <a-modal v-model="show" title="新增专业绑定" @cancel="onClose" :width="600">
    <template slot="footer">
      <a-button key="back" @click="onClose">
        取消
      </a-button>
      <a-button key="submit" type="primary" :loading="loading" @click="handleSubmit">
        提交
      </a-button>
    </template>
    <a-form :form="form" layout="vertical">
      <a-row :gutter="20">
        <a-col :span="24">
          <a-form-item label='选择学校' v-bind="formItemLayout">
            <a-select
              v-decorator="[
              'schoolId',
              { rules: [{ required: true, message: '请输入学校!' }] }
              ]"
              show-search
              placeholder="请选择学校..."
              style="width: 100%"
              :default-active-first-option="false"
              :show-arrow="false"
              :filter-option="false"
              :not-found-content="null"
              @search="handleSearch">
              <a-select-option v-for="d in schoolList" :value="d.id" :key="d.id">
                {{ d.name }}
              </a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="24">
          <a-form-item label='选择专业' v-bind="formItemLayout">
            <a-select
              v-decorator="[
              'disciplineCode',
              { rules: [{ required: true, message: '请输入专业!' }] }
              ]"
              show-search
              placeholder="请选择专业..."
              style="width: 100%"
              :default-active-first-option="false"
              :show-arrow="false"
              :filter-option="false"
              :not-found-content="null"
              @search="disciplineHandleSearch">
              <a-select-option v-for="d in disciplineList" :value="d.code" :key="d.code">
                {{ d.name }}
              </a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="24">
          <a-form-item label='是否特色专业' v-bind="formItemLayout">
            <a-radio-group default-value="否" button-style="solid" v-decorator="[
              'featureFlag',
              { rules: [{ required: true, message: '是否特色专业!' }] }
              ]">
              <a-radio-button value="是">
                是
              </a-radio-button>
              <a-radio-button value="否">
                否
              </a-radio-button>
            </a-radio-group>
          </a-form-item>
        </a-col>
        <a-col :span="24">
          <div style="border: 1px solid #ccc;">
            <Toolbar
              style="border-bottom: 1px solid #ccc"
              :editor="editor"
              :defaultConfig="toolbarConfig"
              :mode="mode"
            />
            <Editor
              style="height: 500px; overflow-y: hidden;"
              v-model="html"
              :defaultConfig="editorConfig"
              :mode="mode"
              @onCreated="onCreated"
            />
          </div>
        </a-col>
      </a-row>
    </a-form>
  </a-modal>
</template>

<script>
import {mapState} from 'vuex'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import moment from "moment";
function getBase64 (file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.readAsDataURL(file)
    reader.onload = () => resolve(reader.result)
    reader.onerror = error => reject(error)
  })
}
const formItemLayout = {
  labelCol: { span: 24 },
  wrapperCol: { span: 24 }
}
export default {
  name: 'bindAdd',
  components: { Editor, Toolbar },
  props: {
    bindAddVisiable: {
      default: false
    }
  },
  computed: {
    ...mapState({
      currentbind: state => state.account.bind
    }),
    show: {
      get: function () {
        return this.bindAddVisiable
      },
      set: function () {
      }
    }
  },
  data () {
    return {
      formItemLayout,
      form: this.$form.createForm(this),
      loading: false,
      fileList: [],
      schoolList: [],
      disciplineList: [],
      previewVisible: false,
      previewImage: '',
      editor: null,
      html: '<p></p>',
      toolbarConfig: { },
      editorConfig: { placeholder: '请输入内容...' },
      mode: 'default'
    }
  },
  beforeDestroy () {
    const editor = this.editor
    if (editor == null) return
    editor.destroy() // 组件销毁时，及时销毁编辑器
  },
  methods: {
    nCreated (editor) {
      this.editor = Object.seal(editor) // 一定要用 Object.seal() ，否则会报错
    },
    handleSearch (value) {
      this.schoolList = []
      if (value !== '' && value !== null) {
        this.$get(`/cos/sys-school/listLikeByKey/${value}`).then((r) => {
          this.schoolList = r.data.data
        })
      }
    },
    disciplineHandleSearch (value) {
      this.disciplineList = []
      if (value !== '' && value !== null) {
        this.$get(`/cos/discipline-info/listLikeByKey/${value}`).then((r) => {
          this.disciplineList = r.data.data
        })
      }
    },
    handleCancel () {
      this.previewVisible = false
    },
    async handlePreview (file) {
      if (!file.url && !file.preview) {
        file.preview = await getBase64(file.originFileObj)
      }
      this.previewImage = file.url || file.preview
      this.previewVisible = true
    },
    picHandleChange ({ fileList }) {
      this.fileList = fileList
    },
    reset () {
      this.loading = false
      this.form.resetFields()
    },
    onClose () {
      this.reset()
      this.$emit('close')
    },
    handleSubmit () {
      // 获取图片List
      let images = []
      this.fileList.forEach(image => {
        images.push(image.response)
      })
      this.form.validateFields((err, values) => {
        values.images = images.length > 0 ? images.join(',') : null
        values.content = this.editor.getHtml()
        // values.birthday = moment(values.birthday).format('YYYY-MM-DD')
        if (!err) {
          this.loading = true
          this.$post('/cos/professional', {
            ...values
          }).then((r) => {
            this.reset()
            this.$emit('success')
          }).catch(() => {
            this.loading = false
          })
        }
      })
    }
  }
}
</script>
<style src="@wangeditor/editor/dist/css/style.css"></style>
<style scoped>

</style>
