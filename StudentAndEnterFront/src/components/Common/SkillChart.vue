<template>
  <div class="skill-chart">
    <h3 class="chart-title">技能雷达图</h3>
    <div class="skills-container">
      <div v-for="(skill, index) in skills" :key="index" class="skill-item">
        <div class="skill-name">{{ skill.name }}</div>
        <div class="skill-bar-container">
          <div class="skill-bar" :style="{ width: (skill.level * 20) + '%' }"></div>
        </div>
        <div class="skill-level">{{ skill.level }}/5</div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'SkillChart',
  props: {
    skills: {
      type: Array,
      required: true,
      // 期望格式: [{ name: 'JavaScript', level: 4 }, { name: 'Java', level: 3 }]
    }
  }
};
</script>

<style scoped>
.skill-chart {
  background-color: white;
  border-radius: 8px;
  padding: 15px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.chart-title {
  text-align: center;
  margin-bottom: 15px;
  color: #333;
  font-size: 18px;
}

.skills-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.skill-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.skill-name {
  width: 100px;
  font-size: 14px;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.skill-bar-container {
  flex: 1;
  height: 10px;
  background-color: #f0f0f0;
  border-radius: 5px;
  overflow: hidden;
}

.skill-bar {
  height: 100%;
  background-color: #1890ff;
  border-radius: 5px;
}

.skill-level {
  width: 40px;
  text-align: right;
  font-size: 14px;
  color: #666;
}
</style>
