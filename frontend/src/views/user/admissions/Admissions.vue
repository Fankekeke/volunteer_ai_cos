<template>
  <div style="width: 100%">
    <a-row style="margin-top: 25px">
      <a-col :span="12">
        <div>
          <a-input-search
            placeholder="学校搜索"
            enter-button="搜索"
            @search="onSearch"
          />
        </div>
      </a-col>
      <a-col :span="12" style="text-align: right">
        <a-radio-group button-style="solid" v-model="checkFlag" style="width: 100%">
          <a-radio-button value="1" style="width: 25%;text-align: center">
            全部招生信息
          </a-radio-button>
          <a-radio-button value="2" style="width: 25%;text-align: center">
            学校专业推荐
          </a-radio-button>
        </a-radio-group>
      </a-col>
      <a-col :span="24" style="margin-top: 25px">
        <div style="background:#ECECEC; padding:30px">
          <a-skeleton :loading="loading" active :paragraph="{ rows: 10 }"/>
          <a-alert v-if="!loading" message="此数据根据学校信息进行统计" type="info" close-text="Close Now"
                   style="margin-bottom: 15px"/>
          <a-row :gutter="15" v-if="!loading">
            <a-col :span="6" v-for="(item, index) in dataList" style="margin-bottom: 15px" :key="index">
              <a-card
                @click="showDetail(item)" :bordered="false"
                hoverable
                :description="item.address"
                class="school-card">
                <span slot="title" class="card-title">
                  <a-badge status="processing"/>
                  <span class="school-name">{{ item.schoolName }}</span>
                  <span class="discipline-name">{{ item.disciplineName }}</span>
                </span>
                <a-row class="card-content">
                  <a-col :span="24">
                    <div class="location-info">
                      <a-icon type="environment"/>
                      {{ item.province }} - {{ item.city }}
                    </div>
                    <div class="school-meta">
                      {{ item.manage }} | {{ item.level }}
                    </div>
                    <div class="school-stats">
                      <a-row :gutter="12">
                        <a-col :span="8">
                          <div class="stat-item">
                            <a-tag :color="item.type == 1 ? 'pink' : 'blue'" class="type-tag">
                              {{ item.type == 1 ? '文科' : '理科' }}
                            </a-tag>
                          </div>
                        </a-col>
                        <a-col :span="8">
                          <div class="stat-item">
                            <a-icon type="hourglass"/>
                            <span class="stat-value">分数：{{ item.score }}分</span>
                          </div>
                        </a-col>
                        <a-col :span="8">
                          <div class="stat-item">
                            <a-icon type="team"/>
                            <span class="stat-value">招生：{{ item.admissions }}人</span>
                          </div>
                        </a-col>
                      </a-row>
                    </div>
                    <div class="apply-button-container">
                      <a-button type="primary" block @click.stop="apply(item)" class="apply-button">
                        志愿申请
                      </a-button>
                    </div>
                  </a-col>
                </a-row>
              </a-card>
            </a-col>
          </a-row>
          <a-pagination show-quick-jumper :defaultCurrent="page.current" :total="page.total"
                        :defaultPageSize="page.size" showLessItems @change="pageChange"/>
        </div>
      </a-col>
    </a-row>
    <a-modal
      :visible="modalVisible"
      title="学校详细信息"
      width="600px"
      @ok="handleModalOk"
      @cancel="handleModalCancel"
    >
      <div v-if="selectedSchool" class="detail-content">
        <a-row :gutter="16">
          <a-col :span="24">
            <h3 class="detail-title">
              {{ selectedSchool.schoolName }}
              <span class="detail-discipline">{{ selectedSchool.disciplineName }}</span>
            </h3>
          </a-col>
          <a-col :span="12">
            <p><a-icon type="environment" /> 地址：{{ selectedSchool.province }} - {{ selectedSchool.city }}</p>
            <p><a-icon type="bank" /> 类型：{{ selectedSchool.manage }} | {{ selectedSchool.level }}</p>
            <p><a-icon type="calendar" /> 年份：{{ selectedSchool.year }}</p>
          </a-col>
          <a-col :span="12">
            <p>
              <a-tag :color="selectedSchool.type == 1 ? 'pink' : 'blue'">
                {{ selectedSchool.type == 1 ? '文科' : '理科' }}
              </a-tag>
            </p>
            <p><a-icon type="hourglass" /> 录取分数线：{{ selectedSchool.score }}分</p>
            <p><a-icon type="team" /> 招生人数：{{ selectedSchool.admissions }}人</p>
          </a-col>
          <a-col :span="24" style="margin-top: 20px">
            <a-divider>专业介绍</a-divider>
            <div v-if="aiLoading" class="ai-loading">
              <a-spin />
              <p style="margin-top: 10px;">正在为您生成专业介绍...</p>
            </div>
            <div v-else class="ai-content">
              <vue-markdown :source="aiResponse" class="markdown-content"></vue-markdown>
            </div>
          </a-col>
        </a-row>
      </div>
    </a-modal>
  </div>
</template>

<script>
import {mapState} from 'vuex'
import VueMarkdown from 'vue-markdown'
export default {
  name: 'House',
  components: {
    VueMarkdown
  },
  data () {
    return {
      aiLoading: false,
      aiResponse: '',
      modalVisible: false,
      selectedSchool: null,
      currentTab: 1,
      page: {
        current: 1,
        total: 0,
        size: 36
      },
      dataList: [],
      loading: false,
      checkFlag: '1',
      series: [{
        name: 'Series 1',
        data: [80, 50, 30, 40, 100, 20]
      }],
      chartOptions: {
        chart: {
          height: 350,
          type: 'radar'
        },
        title: {
          text: 'Basic Radar Chart'
        },
        xaxis: {
          categories: ['January', 'February', 'March', 'April', 'May', 'June']
        }
      }
    }
  },
  computed: {
    ...mapState({
      currentUser: state => state.account.user
    })
  },
  watch: {
    checkFlag: function (value) {
      this.currentTab = value
      this.page.current = 1
      this.page.size = 36
      this.selectSchoolRate(value)
    }
  },
  mounted () {
    this.selectSchoolRate(1)
  },
  methods: {
    generateResponse (item) {
      this.aiLoading = true
      this.aiResponse = ''
      let content = '帮我介绍一下，' + item.schoolName + '的详细信息, 以及' + item.disciplineName + '的详细信息及优劣势，分析之后的就业前景'
      this.$post(`/cos/ai/aliTyqw`, {
        content: content
      }).then((r) => {
        this.aiResponse = r.data.msg
        this.aiLoading = false
      }).catch(() => {
        this.aiResponse = '获取信息失败，请稍后重试'
        this.aiLoading = false
      })
    },
    showDetail (item) {
      this.selectedSchool = item
      this.modalVisible = true
      this.generateResponse(item)
    },

    handleModalOk () {
      this.modalVisible = false
    },

    handleModalCancel () {
      this.modalVisible = false
    },
    apply (row) {
      let params = {schoolId: row.schoolId, userId: this.currentUser.userId}
      this.$post('/cos/apply-bill-info/addApplyBill', params).then((r) => {
        this.$message.success('申请成功')
      })
    },
    onSearch (value) {
      this.page.size = 36
      this.page.current = 1
      this.selectSchoolRate(this.currentTab, value)
    },
    selectSchoolRate (type, schoolName) {
      this.loading = true
      let params = {}
      params.size = this.page.size
      params.current = this.page.current
      if (schoolName) {
        params.schoolName = schoolName
      }
      params.userId = this.currentUser.userId
      if (type == 1) {
        this.$get(`/cos/score-line-info/page`, {...params}).then((r) => {
          let data = r.data.data
          const pagination = {...this.pagination}
          pagination.total = data.total
          this.dataList = data.records
          this.page = pagination
          console.log(this.page)
          // 数据加载完毕，关闭loading
          this.loading = false
        })
      } else {
        this.$get(`/cos/score-line-info/selectRecommendSchool`, params).then((r) => {
          let data = r.data.data
          const pagination = {...this.pagination}
          pagination.total = data.total
          this.dataList = data.records
          this.page = pagination
          console.log(this.page)
          // 数据加载完毕，关闭loading
          this.loading = false
        })
      }
    },
    pageChange (page, pageSize) {
      this.page.size = pageSize
      this.page.current = page
      this.selectSchoolRate(this.currentTab)
    },
    fetch (params = {}) {
      // 显示loading
      this.loading = true
      // 如果分页信息为空，则设置为默认值
      params.size = this.page.size
      params.current = this.page.current
      this.$get('/cos/score-line-info/page', {
        ...params
      }).then((r) => {
        let data = r.data.data
        const pagination = {...this.pagination}
        pagination.total = data.total
        this.dataList = data.records
        this.page = pagination
        console.log(this.page)
        // 数据加载完毕，关闭loading
        this.loading = false
      })
    }
  }
}
</script>

<style scoped>
>>> .ant-card-head-title {
  font-size: 13px;
  font-family: SimHei;
}

>>> .ant-alert-message {
  font-size: 14px;
  font-family: SimHei;
}

.school-card {
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.school-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.school-name {
  font-weight: 600;
  font-size: 14px;
  color: #333;
}

.discipline-name {
  color: #fa541c;
  font-weight: 500;
  margin-left: 5px;
}

.card-content {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.location-info {
  font-size: 13px;
  color: #666;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 5px;
}

.school-meta {
  font-size: 12px;
  color: #888;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px dashed #eee;
}

.school-stats {
  margin-bottom: 15px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #555;
}

.stat-value {
  font-weight: 500;
}

.type-tag {
  font-size: 12px !important;
  padding: 0 6px;
  border-radius: 4px;
}

.apply-button-container {
  margin-top: 10px;
}

.apply-button {
  border-radius: 6px;
  font-weight: 500;
  transition: all 0.3s;
}

.detail-content {
  padding: 20px;
}

.detail-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 20px;
  color: #333;
}

.detail-discipline {
  color: #fa541c;
  font-weight: 500;
  margin-left: 10px;
}

.detail-content p {
  margin-bottom: 10px;
  font-size: 14px;
  color: #666;
}

.detail-content .ant-tag {
  font-size: 12px;
}

.school-card {
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  cursor: pointer;
}

.school-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.ai-loading {
  text-align: center;
  padding: 20px;
}

.ai-content {
  min-height: 100px;
  padding: 10px 0;
}

.markdown-content {
  line-height: 1.6;
  font-size: 14px;
}

.markdown-content h1,
.markdown-content h2,
.markdown-content h3 {
  margin: 15px 0 10px 0;
  color: #333;
}

.markdown-content p {
  margin-bottom: 10px;
  color: #666;
}

.markdown-content ul,
.markdown-content ol {
  padding-left: 20px;
  margin-bottom: 10px;
}

.markdown-content li {
  margin-bottom: 5px;
}

.markdown-content strong {
  color: #333;
}
</style>
