const url_mem = 'http://localhost:8080/mini-os/memory/query-all';

function fetchData() {
    fetch(url_mem)
        .then(response => response.json())
        .then(data => {
            document.addEventListener("DOMContentLoaded", function() {
                // 模拟后端发送的数据
                const backendData_square = [
                    [1, 0, 1, 0, 1, 0, 1, 0],
                    [0, 1, 0, 1, 0, 1, 0, 1],
                    [1, 0, 1, 0, 1, 0, 1, 0],
                    [0, 1, 0, 1, 0, 1, 0, 1],
                    [1, 0, 1, 0, 1, 0, 1, 0],
                    [0, 1, 0, 1, 0, 1, 0, 1],
                    [1, 0, 1, 0, 1, 0, 1, 0],
                    [0, 1, 0, 1, 0, 1, 0, 1]
                ];

                // 获取容器和气泡元素
                const gridContainer = document.getElementById('grid-container');
                const tooltip = document.getElementById('tooltip');

                // 根据后端数据动态生成小正方形
                for (let i = 0; i < 64; i++) {
                    const gridItem = document.createElement('div');
                    gridItem.classList.add('grid-item');
                    if (backendData_square[i][j] === 1) {
                        gridItem.style.backgroundColor = 'rgba(210,180,140,20)'; // 1 对应的颜色
                        gridItem.style.background = 'rgba(210,180,140,20)'; // 1 对应的颜色
                    }
                    gridItem.addEventListener('mouseenter', () => {
                        tooltip.textContent = `(${i}, ${j})`; // 小正方形的坐标信息
                        tooltip.style.left = `${gridItem.offsetLeft}px`;
                        tooltip.style.top = `${gridItem.offsetTop}px`;
                        tooltip.style.display = 'block';
                    });
                    gridItem.addEventListener('mouseleave', () => {
                        tooltip.style.display = 'none';
                    });
                    gridContainer.appendChild(gridItem);
                }
            });
        })
        .catch(error => console.error(error));
}

fetchData();
setInterval(fetchData, 100);
