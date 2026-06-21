<script setup lang="ts">
import { computed, ref, type Component, type CSSProperties } from 'vue'
import { RouterLink } from 'vue-router'
import {
  ArrowRight,
  Binary,
  Boxes,
  Brain,
  Briefcase,
  Bug,
  Code2,
  Coffee,
  Cpu,
  Database,
  FileQuestion,
  GitBranch,
  Globe2,
  GraduationCap,
  Layers,
  Network,
  PanelTop,
  Rocket,
  Server,
  ShieldCheck,
  Sparkles,
  Terminal,
  Workflow,
} from 'lucide-vue-next'
import { Badge } from '../../components/ui/badge'
import { Button } from '../../components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../../components/ui/card'

interface LearningTopic {
  title: string
  category: string
  description: string
  level: string
  questionCount: number
  keyword: string
  tags: string[]
  icon: Component
  accent: string
  accentSoft: string
}

interface InterviewTrack {
  title: string
  description: string
  count: number
  coverage: number
  keyword: string
  icon: Component
}

const categories = ['全部', '计算机基础', 'Java后端', '数据库', '前端', '工程工具', '系统设计', 'AI与安全']
const selectedCategory = ref('全部')

const topics: LearningTopic[] = [
  {
    title: '计算机网络',
    category: '计算机基础',
    description: 'TCP/IP、HTTP、DNS、网关、连接池与线上排障高频知识。',
    level: '基础到进阶',
    questionCount: 42,
    keyword: '计算机网络',
    tags: ['HTTP', 'TCP', '排障'],
    icon: Network,
    accent: 'hsl(214 86% 52%)',
    accentSoft: 'hsl(214 86% 52% / 0.12)',
  },
  {
    title: '操作系统',
    category: '计算机基础',
    description: '进程线程、内存管理、IO 模型、锁与调度机制。',
    level: '基础必修',
    questionCount: 36,
    keyword: '操作系统',
    tags: ['线程', '内存', 'IO'],
    icon: Cpu,
    accent: 'hsl(262 72% 56%)',
    accentSoft: 'hsl(262 72% 56% / 0.12)',
  },
  {
    title: '数据结构与算法',
    category: '计算机基础',
    description: '数组、链表、树、图、排序、动态规划与常见手写题。',
    level: '长期训练',
    questionCount: 58,
    keyword: '数据结构 算法',
    tags: ['树', '图', 'DP'],
    icon: Binary,
    accent: 'hsl(168 78% 34%)',
    accentSoft: 'hsl(168 78% 34% / 0.12)',
  },
  {
    title: '计算机组成原理',
    category: '计算机基础',
    description: 'CPU、缓存、指令、总线与性能分析的底层基础。',
    level: '基础补强',
    questionCount: 24,
    keyword: '计算机组成原理',
    tags: ['CPU', '缓存', '指令'],
    icon: Boxes,
    accent: 'hsl(32 92% 48%)',
    accentSoft: 'hsl(32 92% 48% / 0.13)',
  },
  {
    title: 'Java 核心',
    category: 'Java后端',
    description: '集合、泛型、异常、反射、并发基础与常见源码题。',
    level: '后端主线',
    questionCount: 64,
    keyword: 'Java 核心 面试',
    tags: ['集合', '并发', 'JDK'],
    icon: Coffee,
    accent: 'hsl(18 92% 50%)',
    accentSoft: 'hsl(18 92% 50% / 0.12)',
  },
  {
    title: 'JVM',
    category: 'Java后端',
    description: '类加载、内存区域、GC、性能调优与线上问题定位。',
    level: '进阶重点',
    questionCount: 46,
    keyword: 'JVM 调优',
    tags: ['GC', '类加载', '调优'],
    icon: Rocket,
    accent: 'hsl(346 84% 55%)',
    accentSoft: 'hsl(346 84% 55% / 0.12)',
  },
  {
    title: 'Spring 全家桶',
    category: 'Java后端',
    description: 'Spring Boot、Spring MVC、AOP、事务、Bean 生命周期。',
    level: '项目核心',
    questionCount: 72,
    keyword: 'Spring Boot Spring 面试',
    tags: ['IOC', 'AOP', '事务'],
    icon: Layers,
    accent: 'hsl(142 62% 36%)',
    accentSoft: 'hsl(142 62% 36% / 0.12)',
  },
  {
    title: 'MyBatis-Plus',
    category: 'Java后端',
    description: 'ORM 映射、分页、条件构造器、乐观锁与 SQL 排查。',
    level: '项目常用',
    questionCount: 28,
    keyword: 'MyBatis Plus',
    tags: ['ORM', '分页', 'SQL'],
    icon: FileQuestion,
    accent: 'hsl(199 92% 43%)',
    accentSoft: 'hsl(199 92% 43% / 0.12)',
  },
  {
    title: 'MySQL',
    category: '数据库',
    description: '索引、事务、锁、MVCC、Explain 与慢 SQL 优化。',
    level: '必考重点',
    questionCount: 68,
    keyword: 'MySQL 索引 事务',
    tags: ['索引', '事务', '锁'],
    icon: Database,
    accent: 'hsl(188 92% 34%)',
    accentSoft: 'hsl(188 92% 34% / 0.12)',
  },
  {
    title: 'Redis',
    category: '数据库',
    description: '缓存、分布式锁、持久化、淘汰策略与缓存一致性。',
    level: '高频重点',
    questionCount: 54,
    keyword: 'Redis 缓存 分布式锁',
    tags: ['缓存', '锁', '一致性'],
    icon: Server,
    accent: 'hsl(2 78% 52%)',
    accentSoft: 'hsl(2 78% 52% / 0.12)',
  },
  {
    title: '数据库设计',
    category: '数据库',
    description: '表结构、约束、范式、字段冗余、审计字段与迁移脚本。',
    level: '项目能力',
    questionCount: 31,
    keyword: '数据库设计',
    tags: ['建模', '约束', '迁移'],
    icon: Workflow,
    accent: 'hsl(238 74% 60%)',
    accentSoft: 'hsl(238 74% 60% / 0.12)',
  },
  {
    title: 'Vue 与 TypeScript',
    category: '前端',
    description: '组合式 API、路由、状态管理、类型边界与组件封装。',
    level: '前端主线',
    questionCount: 39,
    keyword: 'Vue TypeScript',
    tags: ['Vue3', 'TS', '组件'],
    icon: Code2,
    accent: 'hsl(153 64% 38%)',
    accentSoft: 'hsl(153 64% 38% / 0.12)',
  },
  {
    title: '前端工程化',
    category: '前端',
    description: 'Vite、构建优化、包管理、环境变量与前端部署。',
    level: '实战补齐',
    questionCount: 26,
    keyword: '前端工程化 Vite',
    tags: ['Vite', '构建', '部署'],
    icon: PanelTop,
    accent: 'hsl(43 94% 45%)',
    accentSoft: 'hsl(43 94% 45% / 0.14)',
  },
  {
    title: 'Linux',
    category: '工程工具',
    description: '常用命令、权限、日志排查、端口进程与服务部署。',
    level: '必会工具',
    questionCount: 33,
    keyword: 'Linux 命令 排查',
    tags: ['命令', '日志', '部署'],
    icon: Terminal,
    accent: 'hsl(215 18% 34%)',
    accentSoft: 'hsl(215 18% 34% / 0.12)',
  },
  {
    title: 'Git / Maven',
    category: '工程工具',
    description: '分支协作、冲突处理、依赖管理、生命周期与构建排错。',
    level: '协作基础',
    questionCount: 29,
    keyword: 'Git Maven',
    tags: ['分支', '依赖', '构建'],
    icon: GitBranch,
    accent: 'hsl(24 88% 52%)',
    accentSoft: 'hsl(24 88% 52% / 0.12)',
  },
  {
    title: 'DevOps / 部署',
    category: '工程工具',
    description: '环境隔离、配置管理、反向代理、日志和上线回滚。',
    level: '展示加分',
    questionCount: 22,
    keyword: 'DevOps 部署',
    tags: ['环境', '代理', '回滚'],
    icon: Globe2,
    accent: 'hsl(204 82% 46%)',
    accentSoft: 'hsl(204 82% 46% / 0.12)',
  },
  {
    title: '系统设计',
    category: '系统设计',
    description: '限流、鉴权、缓存、消息、幂等、可观测性与扩展性。',
    level: '高级面试',
    questionCount: 48,
    keyword: '系统设计 面试',
    tags: ['限流', '缓存', '幂等'],
    icon: Workflow,
    accent: 'hsl(274 72% 55%)',
    accentSoft: 'hsl(274 72% 55% / 0.12)',
  },
  {
    title: '项目场景题',
    category: '系统设计',
    description: '围绕博客项目讲清业务分层、事务、缓存、安全和扩展。',
    level: '简历核心',
    questionCount: 44,
    keyword: '项目场景题',
    tags: ['分层', '事务', '安全'],
    icon: Briefcase,
    accent: 'hsl(12 88% 56%)',
    accentSoft: 'hsl(12 88% 56% / 0.12)',
  },
  {
    title: '高并发专题',
    category: '系统设计',
    description: '线程池、锁、队列、削峰、热点数据与压测思路。',
    level: '进阶专题',
    questionCount: 37,
    keyword: '高并发',
    tags: ['线程池', '队列', '热点'],
    icon: Rocket,
    accent: 'hsl(352 78% 56%)',
    accentSoft: 'hsl(352 78% 56% / 0.12)',
  },
  {
    title: '网络安全',
    category: 'AI与安全',
    description: 'XSS、CSRF、SQL 注入、接口鉴权、限流与敏感信息保护。',
    level: '上线必备',
    questionCount: 32,
    keyword: '网络安全 XSS CSRF',
    tags: ['XSS', '鉴权', '限流'],
    icon: ShieldCheck,
    accent: 'hsl(164 76% 32%)',
    accentSoft: 'hsl(164 76% 32% / 0.12)',
  },
  {
    title: '人工智能基础',
    category: 'AI与安全',
    description: '机器学习、向量检索、提示词、RAG 与大模型应用边界。',
    level: '拓展方向',
    questionCount: 30,
    keyword: '人工智能 大模型',
    tags: ['RAG', '向量', 'Prompt'],
    icon: Brain,
    accent: 'hsl(286 70% 54%)',
    accentSoft: 'hsl(286 70% 54% / 0.12)',
  },
  {
    title: '排错与 Bug 分析',
    category: '工程工具',
    description: '从日志、复现、定位、修复、验证到复盘的完整链路。',
    level: '工程能力',
    questionCount: 25,
    keyword: 'Bug 排查',
    tags: ['日志', '复现', '复盘'],
    icon: Bug,
    accent: 'hsl(332 74% 52%)',
    accentSoft: 'hsl(332 74% 52% / 0.12)',
  },
]

const interviewTracks: InterviewTrack[] = [
  {
    title: 'Java 八股速查',
    description: '集合、并发、JVM、Spring 的高频问答路线。',
    count: 126,
    coverage: 78,
    keyword: 'Java 面试题',
    icon: Coffee,
  },
  {
    title: '数据库专项',
    description: 'MySQL 索引事务、Redis 缓存、慢 SQL 优化。',
    count: 92,
    coverage: 72,
    keyword: '数据库 面试题',
    icon: Database,
  },
  {
    title: '项目场景追问',
    description: '围绕 TechNote 项目讲清分层、事务和扩展。',
    count: 68,
    coverage: 64,
    keyword: '项目面试',
    icon: Briefcase,
  },
  {
    title: '系统设计题',
    description: '限流、缓存、权限、消息、幂等和可观测性。',
    count: 74,
    coverage: 58,
    keyword: '系统设计题',
    icon: Workflow,
  },
  {
    title: '手写与算法',
    description: '常见数据结构、排序、LRU、线程池和工具函数。',
    count: 83,
    coverage: 61,
    keyword: '手写题 算法',
    icon: Binary,
  },
  {
    title: '综合模拟面',
    description: '按真实面试节奏串联自我介绍、项目和技术追问。',
    count: 36,
    coverage: 46,
    keyword: '模拟面试',
    icon: GraduationCap,
  },
]

const roadmapSteps = [
  { title: '基础打底', text: '先补网络、操作系统、数据结构和 Java 语法边界。' },
  { title: '后端主线', text: '围绕 Spring、MyBatis、MySQL、Redis 做可讲清楚的项目闭环。' },
  { title: '工程实战', text: '补齐 Linux、Git、Maven、部署、日志和线上排障经验。' },
  { title: '面试冲刺', text: '按八股、项目、场景、系统设计四类反复压测表达。' },
]

const filteredTopics = computed(() =>
  selectedCategory.value === '全部' ? topics : topics.filter((topic) => topic.category === selectedCategory.value),
)

const totalQuestionCount = computed(() => topics.reduce((total, topic) => total + topic.questionCount, 0))

const searchTo = (keyword: string) => ({
  path: '/search',
  query: { keyword },
})

const topicStyle = (topic: LearningTopic) =>
  ({
    '--topic-accent': topic.accent,
    '--topic-accent-soft': topic.accentSoft,
  }) as CSSProperties
</script>

<template>
  <div class="flex flex-col gap-5">
    <section class="roadmap-hero overflow-hidden rounded-lg border p-5 text-white sm:p-7">
      <div class="flex flex-col gap-6 lg:flex-row lg:items-end lg:justify-between">
        <div class="max-w-2xl">
          <div class="mb-4 flex items-center gap-2 text-sm font-medium text-white/85">
            <Sparkles class="size-4" data-icon="inline-start" />
            TechNote 学习中枢
          </div>
          <h1 class="break-words text-3xl font-semibold leading-tight sm:text-4xl">技术学习路线与面试题库</h1>
          <p class="mt-3 max-w-xl break-words text-sm leading-7 text-white/85 sm:text-base">
            把后端、数据库、前端、工程工具、系统设计和项目场景题集中到一个入口，学习、复盘和面试准备都从这里开始。
          </p>
        </div>
        <div class="grid gap-3 sm:grid-cols-3 lg:min-w-[420px]">
          <div class="rounded-lg border border-white/20 bg-white/15 p-4 backdrop-blur">
            <div class="text-2xl font-semibold">{{ topics.length }}</div>
            <div class="mt-1 text-sm text-white/80">技术方向</div>
          </div>
          <div class="rounded-lg border border-white/20 bg-white/15 p-4 backdrop-blur">
            <div class="text-2xl font-semibold">{{ totalQuestionCount }}</div>
            <div class="mt-1 text-sm text-white/80">面试题点</div>
          </div>
          <div class="rounded-lg border border-white/20 bg-white/15 p-4 backdrop-blur">
            <div class="text-2xl font-semibold">{{ categories.length - 1 }}</div>
            <div class="mt-1 text-sm text-white/80">学习分类</div>
          </div>
        </div>
      </div>
    </section>

    <Card>
      <CardHeader>
        <div class="flex flex-wrap items-start justify-between gap-3">
          <div>
            <CardTitle>热门方向</CardTitle>
            <CardDescription>按方向筛选技术卡片，优先覆盖后端学习和面试高频内容。</CardDescription>
          </div>
          <Badge variant="secondary">{{ filteredTopics.length }} 个方向</Badge>
        </div>
      </CardHeader>
      <CardContent>
        <div class="flex flex-wrap gap-2" aria-label="学习方向筛选">
          <Button
            v-for="category in categories"
            :key="category"
            size="sm"
            :variant="selectedCategory === category ? 'default' : 'secondary'"
            class="rounded-full"
            :aria-pressed="selectedCategory === category"
            @click="selectedCategory = category"
          >
            {{ category }}
          </Button>
        </div>
      </CardContent>
    </Card>

    <section class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
      <Card
        v-for="topic in filteredTopics"
        :key="topic.title"
        class="topic-card overflow-hidden bg-background transition-colors hover:border-foreground/20"
        :style="topicStyle(topic)"
      >
        <CardHeader>
          <div class="flex items-start justify-between gap-3">
            <div class="topic-icon flex size-16 shrink-0 items-center justify-center rounded-lg">
              <component :is="topic.icon" />
            </div>
            <Badge variant="outline">{{ topic.category }}</Badge>
          </div>
          <CardTitle class="break-words text-lg">{{ topic.title }}</CardTitle>
          <CardDescription class="line-clamp-2 min-h-10">{{ topic.description }}</CardDescription>
        </CardHeader>
        <CardContent class="flex flex-col gap-4">
          <div class="flex flex-wrap gap-2">
            <Badge v-for="tag in topic.tags" :key="`${topic.title}-${tag}`" variant="secondary">{{ tag }}</Badge>
          </div>
          <div class="flex items-center justify-between gap-3 text-sm">
            <span class="text-muted-foreground">{{ topic.level }}</span>
            <span class="font-medium">{{ topic.questionCount }} 题点</span>
          </div>
          <RouterLink
            :to="searchTo(topic.keyword)"
            class="inline-flex h-9 items-center justify-center gap-2 rounded-md border bg-background px-3 text-sm font-medium transition-colors hover:bg-hover [&_svg]:size-4 [&_svg]:shrink-0"
          >
            查看相关文章
            <ArrowRight data-icon="inline-end" />
          </RouterLink>
        </CardContent>
      </Card>
    </section>

    <section class="grid gap-4 lg:grid-cols-[1.2fr_0.8fr]">
      <Card>
        <CardHeader>
          <CardTitle>面试题训练</CardTitle>
          <CardDescription>把题目按真实面试会追问的方向拆开，方便后续接入题库数据。</CardDescription>
        </CardHeader>
        <CardContent class="grid gap-3 md:grid-cols-2">
          <RouterLink
            v-for="track in interviewTracks"
            :key="track.title"
            :to="searchTo(track.keyword)"
            class="group rounded-lg border bg-background p-4 transition-colors hover:bg-hover"
          >
            <div class="flex items-start gap-3">
              <div class="flex size-11 shrink-0 items-center justify-center rounded-md bg-surface text-primary [&_svg]:size-5">
                <component :is="track.icon" />
              </div>
              <div class="min-w-0 flex-1">
                <div class="flex flex-wrap items-center justify-between gap-2">
                  <h2 class="break-words text-base font-semibold group-hover:text-primary">{{ track.title }}</h2>
                  <Badge variant="outline">{{ track.count }} 题</Badge>
                </div>
                <p class="mt-2 break-words text-sm leading-6 text-muted-foreground">{{ track.description }}</p>
                <div class="mt-3 h-2 overflow-hidden rounded-full bg-surface">
                  <div class="h-full rounded-full bg-primary" :style="{ width: `${track.coverage}%` }"></div>
                </div>
              </div>
            </div>
          </RouterLink>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>推荐学习阶段</CardTitle>
          <CardDescription>按照基础、后端、工程、面试四段推进，避免只背题不成体系。</CardDescription>
        </CardHeader>
        <CardContent class="flex flex-col gap-3">
          <div v-for="(step, index) in roadmapSteps" :key="step.title" class="flex gap-3 rounded-lg border bg-background p-4">
            <div class="flex size-8 shrink-0 items-center justify-center rounded-md bg-primary text-sm font-semibold text-primary-foreground">
              {{ index + 1 }}
            </div>
            <div class="min-w-0">
              <h2 class="break-words font-semibold">{{ step.title }}</h2>
              <p class="mt-1 break-words text-sm leading-6 text-muted-foreground">{{ step.text }}</p>
            </div>
          </div>
        </CardContent>
      </Card>
    </section>
  </div>
</template>

<style scoped>
.roadmap-hero {
  background:
    radial-gradient(circle at 16% 18%, rgb(255 255 255 / 0.24), transparent 25%),
    linear-gradient(135deg, hsl(28 100% 68%), hsl(12 93% 60%) 58%, hsl(8 88% 55%));
}

.topic-card {
  --topic-accent: hsl(var(--primary));
  --topic-accent-soft: hsl(var(--primary) / 0.12);
}

.topic-icon {
  background: var(--topic-accent-soft);
  color: var(--topic-accent);
  transition:
    transform 160ms ease,
    background-color 160ms ease;
}

.topic-icon :deep(svg) {
  width: 2.25rem;
  height: 2.25rem;
  stroke-width: 2;
}

.topic-card:hover .topic-icon {
  transform: translateY(-2px);
}

@media (prefers-reduced-motion: reduce) {
  .topic-icon {
    transition: none;
  }

  .topic-card:hover .topic-icon {
    transform: none;
  }
}
</style>
