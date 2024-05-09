const url_process = 'http://localhost:8080/mini-os/process/query-all';

var noDataInterval = 0;
// 发送请求并处理返回的数据
function fetchData() {
    fetch(url_process)
        .then(response => response.json())
        .then(data => {
            console.log('进程模块刷新成功');
            const currentTimeMillis = new Date().getTime();
            const runningProcesses = data.data.runningQueue;
            const waitingQueue = data.data.waitingQueue;
            const readyQueue = data.data.readyQueue;
            // 获取各个数据
            for (let i = 1; i <= runningProcesses.length; i++) {
                const process = runningProcesses[i];
                const pid = process.pid;
                const processName = process.processName;
                const currInst = process.instructions[process.ir - 1];
                const startTime = process.startTime;
                const runTime = (currentTimeMillis - startTime) / 1000;

                // Update the existing HTML elements with new data
                document.getElementById(`cell_r${i}_c1`).textContent = pid;
                document.getElementById(`cell_r${i}_c2`).textContent = processName;
                document.getElementById(`cell_r${i}_c3`).textContent = currInst;
            }
            for (let i = 1; i <= 4; i++) {
                // Update the existing HTML elements with new data
                document.getElementById(`cell_r${i}_c1`).textContent = "";
                document.getElementById(`cell_r${i}_c2`).textContent = "";
                document.getElementById(`cell_r${i}_c3`).textContent = "";
            }

            updateQueue(waitingQueue, readyQueue);

        })
        .catch(error => console.error(error));
}

// 第一次调用 fetchData 函数，用于页面加载时立即获取数据。
fetchData();
// 使用 setInterval 函数定时调用 fetchData 函数，间隔时间为 24/1000 毫秒
setInterval(fetchData, 100);

// 更新队列函数
function updateQueue(waiting_queueData,ready_queueData) {
    const w_queueContainer = document.getElementById('waiting-queue-container');
    const r_queueContainer = document.getElementById('ready-queue-container');
    w_queueContainer.innerHTML = ''; // 清空队列容器
    r_queueContainer.innerHTML = ''; // 清空队列容器

    waiting_queueData.forEach(item => {
        const square = document.createElement('span');
        square.classList.add('square');
        square.textContent = item;
        w_queueContainer.appendChild(square);
    });

    ready_queueData.forEach(item => {
        const square = document.createElement('span');
        square.classList.add('square');
        square.textContent = item;
        r_queueContainer.appendChild(square);
    });
}
