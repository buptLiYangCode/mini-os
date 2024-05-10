const url_process = 'http://localhost:8080/mini-os/process/query-all';

var noDataInterval = 0;
// 发送请求并处理返回的数据
function fetchData() {
    fetch(url_process)
        .then(response => response.json())
        .then(data => {
            console.log('进程模块刷新成功');
            const runningProcesses = data.data.runningQueue;
            const waitingQueue = data.data.waitingQueue;
            const readyQueue1 = data.data.readyQueue[1];
            console.log("find BUg1----"+readyQueue1);
            const readyQueue2 = data.data.readyQueue[2];
            console.log("find BUg2----"+readyQueue2);
            const readyQueue3 = data.data.readyQueue[3];
            console.log("find BUg3----"+readyQueue3);
            // 获取各个数据
            for (let i = 0; i < runningProcesses.length; i++) {
                const process = runningProcesses[i];
                const pid = process.pid;
                const processName = process.processName;
                const currInst = (process.ir - 1) + "：" + process.instructions[process.ir - 1];
                const remainingTime = process.remainingTime;
                const priority = process.priority;

                // Update the existing HTML elements with new data
                document.getElementById(`pid_txt${i}`).textContent = pid;
                document.getElementById(`processName_txt${i}`).textContent = processName;
                document.getElementById(`currInst_txt${i}`).textContent = currInst;
                document.getElementById(`remainingTime_txt${i}`).textContent = remainingTime;
                document.getElementById(`priority_txt${i}`).textContent = priority;
            }
            for (let i = 3; i >= runningProcesses.length; i--) {
                // Update the existing HTML elements with new data
                document.getElementById(`pid_txt${i}`).textContent = "";
                document.getElementById(`processName_txt${i}`).textContent = "";
                document.getElementById(`currInst_txt${i}`).textContent = "";
                document.getElementById(`remainingTime_txt${i}`).textContent = "";
                document.getElementById(`priority_txt${i}`).textContent = "";
            }

            updateQueue(waitingQueue, readyQueue1, readyQueue2, readyQueue3);

        })
        .catch(error => console.error(error));
}

// 第一次调用 fetchData 函数，用于页面加载时立即获取数据。
fetchData();
// 使用 setInterval 函数定时调用 fetchData 函数，间隔时间为 24/1000 毫秒
setInterval(fetchData, 100);

// 更新队列函数
function updateQueue(waiting_queueData,ready_queueData1, ready_queueData2, ready_queueData3, ) {
    console.log("maxiaotiao")

    const w_queueContainer = document.getElementById('waiting-queue-container');
    const r_queueContainer1 = document.getElementById('ready-queue-container1');
    const r_queueContainer2 = document.getElementById('ready-queue-container2');
    const r_queueContainer3 = document.getElementById('ready-queue-container3');
    w_queueContainer.innerHTML = ''; // 清空队列容器
    r_queueContainer1.innerHTML = ''; // 清空队列容器
    r_queueContainer2.innerHTML = ''; // 清空队列容器
    r_queueContainer3.innerHTML = ''; // 清空队列容器

    waiting_queueData.forEach(item => {
        const square = document.createElement('span');
        square.classList.add('square');
        square.textContent = item;
        w_queueContainer.appendChild(square);
    });

    ready_queueData1.forEach(item => {
        console.log("bug 2 ----"+item)
        const square = document.createElement('span');
        square.classList.add('square');
        square.textContent = item;
        r_queueContainer1.appendChild(square);
    });
    ready_queueData2.forEach(item => {
        const square = document.createElement('span');
        square.classList.add('square');
        square.textContent = item;
        r_queueContainer2.appendChild(square);
    });
    ready_queueData3.forEach(item => {
        const square = document.createElement('span');
        square.classList.add('square');
        square.textContent = item;
        r_queueContainer3.appendChild(square);
    });
}