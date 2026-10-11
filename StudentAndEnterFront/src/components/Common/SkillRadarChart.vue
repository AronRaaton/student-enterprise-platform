<template>
  <div class="skill-radar-chart">
    <canvas ref="radarChart" width="150" height="150"></canvas>
  </div>
</template>

<script>
import Chart from 'chart.js/auto';

export default {
  name: 'SkillRadarChart',
  props: {
    skills: {
      type: Array,
      required: true,
      // 期望格式: [{ name: 'JavaScript', level: 4 }, { name: 'Java', level: 3 }]
    }
  },
  data() {
    return {
      chart: null
    };
  },
  mounted() {
    this.createChart();
  },
  methods: {
    createChart() {
      const ctx = this.$refs.radarChart.getContext('2d');

      // 准备数据
      const labels = this.skills.map(skill => skill.name);
      const data = this.skills.map(skill => skill.level);

      // 创建图表
      this.chart = new Chart(ctx, {
        type: 'radar',
        data: {
          labels: labels,
          datasets: [{
            label: '技能水平',
            data: data,
            backgroundColor: 'rgba(54, 162, 235, 0.2)',
            borderColor: 'rgba(54, 162, 235, 1)',
            pointBackgroundColor: 'rgba(54, 162, 235, 1)',
            pointBorderColor: '#fff',
            pointHoverBackgroundColor: '#fff',
            pointHoverBorderColor: 'rgba(54, 162, 235, 1)',
            pointRadius: 2,
            borderWidth: 1
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: true,
          scales: {
            r: {
              angleLines: {
                display: true,
                color: 'rgba(0, 0, 0, 0.1)',
              },
              ticks: {
                display: false,
                backdropPadding: 0
              },
              pointLabels: {
                font: {
                  size: 8
                }
              },
              suggestedMin: 0,
              suggestedMax: 5
            }
          },
          plugins: {
            legend: {
              display: false
            },
            tooltip: {
              callbacks: {
                label: function(context) {
                  return `${context.label}: ${context.raw}/5`;
                }
              }
            }
          }
        }
      });
    }
  },
  watch: {
    skills: {
      handler() {
        if (this.chart) {
          this.chart.destroy();
        }
        this.$nextTick(() => {
          this.createChart();
        });
      },
      deep: true
    }
  },
  beforeDestroy() {
    if (this.chart) {
      this.chart.destroy();
    }
  }
};
</script>

<style scoped>
.skill-radar-chart {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: flex-start;
  align-items: center;
  max-width: 150px;
  max-height: 150px;
  margin: 0;
}
</style>
