
<!-- ... existing code ... -->

<template>
  <div class="work-container">
    <a-row :gutter="24" class="content-row">
      <a-col :span="16" class="carousel-col">
        <div class="carousel-wrapper">
          <a-carousel
            effect="fade"
            autoplay
            :dot-position="'bottom'"
            class="custom-carousel"
          >
            <div v-for="(item, index) in homeImage" :key="index" class="carousel-item">
              <div class="image-overlay"></div>
              <img
                :src="'http://127.0.0.1:9527/imagesWeb/' + item"
                alt="轮播图"
                class="carousel-image"
              />
              <div class="carousel-caption">
                <h3 class="caption-title">欢迎使用高考志愿填报AI分析平台</h3>
              </div>
            </div>
          </a-carousel>

          <!-- 自定义指示器 -->
          <div class="carousel-indicators">
            <span
              v-for="(item, index) in homeImage"
              :key="index"
              :class="['indicator', { active: currentSlide === index }]"
              @click="goToSlide(index)"
            ></span>
          </div>
        </div>
      </a-col>
      <a-col :span="8" class="bulletin-col">
        <a-card
          hoverable
          :loading="loading"
          :bordered="false"
          class="bulletin-card"
          :body-style="{ padding: '0' }"
        >
          <template slot="title" class="bulletin-title">
            <div class="title-content">
              <a-icon type="notification" theme="filled" style="color: #1890ff; margin-right: 10px; font-size: 18px;" />
              <span class="title-text">公告信息</span>
              <a-badge :count="bulletinList.length" :numberStyle="{ backgroundColor: '#52c41a' }" style="margin-left: auto;" />
            </div>
          </template>

          <div class="bulletin-content">
            <a-list
              item-layout="vertical"
              :pagination="false"
              :data-source="bulletinList"
              class="bulletin-list"
            >
              <a-list-item
                slot="renderItem"
                slot-scope="item, index"
                key="item.id"
                class="bulletin-item"
              >
                <template slot="actions">
                  <span key="message" class="bulletin-message">
                    <a-icon type="file-text" theme="filled" style="color: #faad14; margin-right: 8px;" />
                    <span class="bulletin-title-text">
                      {{ item.title ? (item.title.length > 30 ? item.title.substring(0, 30) + '...' : item.title) : '' }}
                    </span>
                  </span>
                </template>

                <a-list-item-meta class="bulletin-meta">
                  <div slot="description" class="bulletin-description">
                    <a-typography-paragraph
                      :ellipsis="{ rows: 2, expandable: false }"
                      :content="item.content"                      style="margin-bottom: 0; color: #666;"
                    />
                  </div>
                </a-list-item-meta>

                <div class="bulletin-actions">
                  <a class="action-link" @click="orderViewOpen(item)">
                    <a-icon type="eye" /> 查看详情
                  </a>
                  <span class="bulletin-date">{{ item.createDate || item.createTime }}</span>
                </div>
              </a-list-item>

              <!-- 空状态 -->
              <!--              <div v-if="bulletinList.length === 0" class="empty-state">-->
              <!--                <a-empty description="暂无公告信息" :image="simpleImage">-->
              <!--                  <a-icon type="inbox" style="font-size: 64px; color: #d9d9d9;" />-->
              <!--                </a-empty>-->
              <!--              </div>-->
            </a-list>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 热门院校卡片 -->
    <a-card
      hoverable
      :loading="schoolLoading"
      :bordered="false"
      class="hot-school-card"          style="margin-top: 24px;"
      :body-style="{ padding: '0' }"
    >
      <template slot="title" class="school-title">
        <div class="title-content">
          <a-icon type="star" theme="filled" style="color: #faad14; margin-right: 10px; font-size: 18px;" />
          <span class="title-text">热门院校</span>
          <a-badge :count="topSchoolList.length" :numberStyle="{ backgroundColor: '#ff4d4f' }" style="margin-left: auto;" />
        </div>
      </template>

      <div class="school-content">
        <a-list
          item-layout="horizontal"
          :pagination="false"
          :data-source="topSchoolList"
          class="school-list"
        >
          <a-list-item
            slot="renderItem"
            slot-scope="item, index"
            :key="item.schoolId"
            class="school-item"
          >
            <a-list-item-meta>
              <template slot="avatar">
                <div :class="['rank-badge', getRankClass(index)]">
                  {{ index + 1 }}
                </div>
              </template>
              <template slot="description">
                <div class="school-info">
                  <div class="school-name">{{ item.schoolName }}</div>
                  <div class="school-tags">
                    <a-tag color="blue" size="small">{{ item.province }}</a-tag>
                    <a-tag color="green" size="small">{{ item.schoolLevel }}</a-tag>
                    <a-tag color="orange" size="small">{{ item.manageType }}</a-tag>
                  </div>
                </div>
              </template>
            </a-list-item-meta>

            <div class="wish-count">
              <a-statistic
                :value="item.wishCount"
                :value-style="{ fontSize: '18px', fontWeight: 'bold', color: '#faad14' }"
              >
                <template slot="prefix">
                  <a-icon type="heart" theme="filled" style="color: #ff4d4f;" />
                </template>
                <template slot="suffix">
                  <span style="font-size: 12px; color: #8c8c8c;">人填报</span>
                </template>
              </a-statistic>
            </div>
          </a-list-item>

          <!-- 空状态 -->
          <div v-if="topSchoolList.length === 0" class="empty-state">
            <a-empty description="暂无热门院校数据" />
          </div>
        </a-list>
      </div>
    </a-card>

    <!-- 详情弹窗 -->
    <a-modal
      v-model="detailVisible"
      title="公告详情"
      :footer="null"
      width="720px"
      class="detail-modal"
    >
      <div class="detail-content" v-if="currentBulletin">
        <div class="detail-header">
          <h3 class="detail-title">{{ currentBulletin.title }}</h3>
          <div class="detail-info">
            <span><a-icon type="user" /> {{ currentBulletin.publisher || '管理员' }}</span>
            <span><a-icon type="clock-circle" /> {{ currentBulletin.date }}</span>
          </div>
        </div>
        <a-divider />
        <div class="detail-body" v-html="currentBulletin.content"></div>
      </div>
    </a-modal>
  </div>
</template>

<script>

export default {
  name: 'Work',
  components: {},
  data () {
    return {
      loading: false,
      schoolLoading: false,
      homeImage: ['http://127.0.0.1:9527/imagesWeb/SA1772884328088.jpg'],
      bulletinList: [],
      topSchoolList: [],
      detailVisible: false,
      currentBulletin: null,
      currentSlide: 0,
    }
  },
  mounted () {
    this.loadData()
    this.queryWishTopSchool()
  },
  methods: {
    queryWishTopSchool () {
      this.schoolLoading = true
      this.$get(`/cos/user-wish-info/queryWishTopSchool`).then((r) => {
        this.topSchoolList = r.data.data
        this.schoolLoading = false
      }).catch(() => {
        this.schoolLoading = false
      })
    },
    loadData () {
      this.$get(`/cos/home-info/data`).then((r) => {
        this.homeImage = r.data.home.images.split(',')
        this.bulletinList = r.data.bulletin
      })
    },
    getRankClass (index) {
      const rankClasses = ['rank-first', 'rank-second', 'rank-third']
      return index < 3 ? rankClasses[index] : ''
    },
    orderViewOpen (item) {
      this.currentBulletin = item
      this.detailVisible = true
    },
    goToSlide (index) {
      this.currentSlide = index
    }
  }
}
</script>

<style lang="less" scoped>.work-container {
  padding: 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: calc(100vh - 188px);
}

.content-row {
  margin: 0 auto;
}

// 轮播图样式
.carousel-col {
  padding-right: 12px;
}

.carousel-wrapper {
  position: relative;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  transition: all 0.3s ease;

  &:hover {
    box-shadow: 0 12px 48px rgba(0, 0, 0, 0.18);
    transform: translateY(-4px);
  }
}

.custom-carousel {
  :deep(.slick-slider) {
    .slick-dots {
      bottom: 20px;

      li {
        button {
          &::before {
            font-size: 10px;
            opacity: 0.6;
            transition: all 0.3s ease;
          }
        }

        &.slick-active button::before {
          opacity: 1;
          color: #fff;
        }
      }
    }
  }
}

.carousel-item {
  position: relative;
  width: 100%;
  height: 550px;
  overflow: hidden;
}

.carousel-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s ease;
}

.carousel-item:hover .carousel-image {
  transform: scale(1.05);
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(
    to bottom,
    rgba(0, 0, 0, 0.1) 0%,
    rgba(0, 0, 0, 0.3) 50%,
    rgba(0, 0, 0, 0.6) 100%
  );
  z-index: 1;
}

.carousel-caption {
  position: absolute;
  bottom: 60px;
  left: 30px;
  right: 30px;
  z-index: 2;
}

.caption-title {
  color: #fff;
  font-size: 28px;
  font-weight: 600;
  text-shadow: 2px 2px 8px rgba(0, 0, 0, 0.5);
  margin: 0;
  animation: fadeInUp 0.8s ease;
}

.carousel-indicators {
  position: absolute;
  bottom: 20px;
  right: 30px;
  z-index: 3;
  display: flex;
  gap: 10px;
}

.indicator {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.5);
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    background: rgba(255, 255, 255, 0.8);
    transform: scale(1.2);
  }

  &.active {
    background: #fff;
    width: 28px;
    border-radius: 6px;
  }
}

// 公告卡片样式
.bulletin-col {
  padding-left: 12px;
}

.bulletin-card {
  height: 550px;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  background: #fff;

  &:hover {
    box-shadow: 0 12px 48px rgba(0, 0, 0, 0.15);
    transform: translateY(-2px);
  }
}

.bulletin-title {
  .title-content {
    display: flex;
    align-items: center;
    padding: 16px 24px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 16px 16px 0 0;

    .title-text {
      font-size: 18px;
      font-weight: 600;
      color: #fff;
    }
  }
}

.bulletin-content {
  height: calc(550px - 70px);
  overflow-y: auto;
  padding: 16px;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background: #d9d9d9;
    border-radius: 3px;

    &:hover {
      background: #bfbfbf;
    }
  }
}

.bulletin-list {
  .bulletin-item {
    padding: 16px;
    margin-bottom: 12px;
    background: #fafafa;
    border-radius: 12px;
    border: 1px solid #f0f0f0;
    transition: all 0.3s ease;

    &:hover {
      background: #fff;
      border-color: #667eea;
      box-shadow: 0 4px 16px rgba(102, 126, 234, 0.15);
      transform: translateX(4px);
    }

    .bulletin-message {
      font-size: 14px;
      font-weight: 500;

      .bulletin-title-text {
        color: #262626;
        font-weight: 600;
      }
    }

    .bulletin-meta {
      margin-top: 8px;

      .bulletin-description {
        font-size: 13px;
        line-height: 1.6;
      }
    }

    .bulletin-actions {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-top: 12px;
      padding-top: 12px;
      border-top: 1px solid #f0f0f0;

      .action-link {
        color: #1890ff;
        font-size: 13px;
        font-weight: 500;
        transition: all 0.3s ease;

        &:hover {
          color: #40a9ff;
          transform: translateX(4px);
        }
      }

      .bulletin-date {
        font-size: 12px;
        color: #8c8c8c;
      }
    }
  }
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
}

// 详情弹窗样式
.detail-modal {
  :deep(.ant-modal-header) {
    border-radius: 12px 12px 0 0;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);

    .ant-modal-title {
      color: #fff;
      font-weight: 600;
    }
  }
}

.detail-content {
  .detail-header {
    margin-bottom: 16px;

    .detail-title {
      font-size: 22px;
      font-weight: 600;
      color: #262626;
      margin-bottom: 12px;
    }

    .detail-info {
      display: flex;
      gap: 24px;
      color: #8c8c8c;
      font-size: 13px;

      span {
        display: flex;
        align-items: center;
        gap: 6px;
      }
    }
  }

  .detail-body {
    font-size: 15px;
    line-height: 1.8;
    color: #595959;
    padding: 16px 0;
  }
}

// 动画
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 响应式
@media (max-width: 1200px) {
  .content-row {
    flex-direction: column;
  }

  .carousel-col,
  .bulletin-col {
    padding: 0;
    margin-bottom: 24px;
  }

  .carousel-item,
  .bulletin-card {
    height: 400px;
  }
}
</style>
<!-- ... existing code ... -->
