const url_device = 'http://localhost:8080/mini-os/device/query-all';

var last_deviceInfoList;

function fetchData() {
    fetch(url_device)
        .then(response => response.json())
        .then(data => {
            console.log('设备块获取成功');
            const deviceInfoList = data.data.deviceInfoList;
            clearTbody(); // 清空上次的内容

            if (last_deviceInfoList !== deviceInfoList) {
                // 检查所有ioRequestQueue是否都为空
                // const allQueuesEmpty = deviceInfoList.every(deviceInfo => deviceInfo.ioRequestQueue.length === 0);
                // if (!allQueuesEmpty) {
                var i = 0;
                // 遍历设备信息列表，创建表格
                deviceInfoList.forEach(deviceInfo => {
                    // 创建标题
                    // const caption = document.createElement('caption');
                    // caption.textContent = `Device State: ${deviceInfo.deviceState}`;
                    // table.appendChild(caption);
                    // 创建表格内容
                    const tbody = document.createElement('tbody');
                    
                    // 各设备状态行
                    const deviceState = deviceInfo.deviceState;
                    const row = document.createElement('tr');
                    const cell = document.createElement('td');
                    if (deviceState === 'DEVICE_READY') {
                        cell.textContent = '设备就绪。';
                    } else if (deviceState === 'DEVICE_WORKING') {
                        cell.textContent = '设备正在工作。';
                    }
                    row.appendChild(cell);
                    tbody.appendChild(row);
                    
                    // 各设备队列详情
                    const ioRequestQueue = deviceInfo.ioRequestQueue;
                    if (ioRequestQueue.length > 0) {
                        // 创建表头
                        // const thead = document.createElement('thead');
                        // const headerRow = document.createElement('tr');
                        // const headerCell = document.createElement('th');
                        // headerCell.textContent = 'Request';
                        // headerRow.appendChild(headerCell);
                        // thead.appendChild(headerRow);
                        // table.appendChild(thead);
                        ioRequestQueue.forEach(request => {
                            // 注意本组给的格式，李洋给了整个PCB
                            const processName = request.pcb.processName;
                            const row = document.createElement('tr');
                            const cell = document.createElement('td');
                            cell.textContent = processName; // 请求格式为字符串
                            row.appendChild(cell);
                            tbody.appendChild(row);
                            
                        });
                        if (ioRequestQueue.length < 5) {
                            for (let j = 0; j < 5 - ioRequestQueue.length; j++) {
                                const row = document.createElement('tr');
                                const cell = document.createElement('td');
                                cell.textContent = ' ';
                                row.appendChild(cell);
                                tbody.appendChild(row);
                            }
                        }
                    } else {
                        // 如果队列为空，显示提示信息
                        appendEmptyQueue(tbody);
                    }
                    // 将表格添加到表头之后
                    if (i === 0) {
                        document.getElementById('kb1').appendChild(tbody);
                    } else if (i === 1) {
                        document.getElementById('kb2').appendChild(tbody);
                    } else if (i === 2) {
                        document.getElementById('printer').appendChild(tbody);
                    // } else if (i === 3) {
                    //     document.getElementById('null').appendChild(tbody);
                    }
                    i++;
                });

                // } else {
                //         // 将表格添加到表头之后
                //         document.getElementById('kb1').appendChild(appendEmptyQueue());
                //         document.getElementById('kb2').appendChild(appendEmptyQueue());
                //         document.getElementById('printer').appendChild(appendEmptyQueue());
                //     }
                last_deviceInfoList = deviceInfoList;
            }
        })
        .catch(error => console.error(error));
}

// 每600ms调用fetchData函数获取数据并显示
setInterval(fetchData, 100);

// 清空tbody的内容，保留thead
function clearTbody() {
    const tbody_kb1 = document.querySelector('#kb1 tbody');
    const tbody_kb2 = document.querySelector('#kb2 tbody');
    const tbody_printer = document.querySelector('#printer tbody');

    if (tbody_kb1) {
        tbody_kb1.remove(); // 删除tbody元素
    }
    if (tbody_kb2) {
        tbody_kb2.remove(); // 删除tbody元素
    }
    if (tbody_printer) {
        tbody_printer.remove(); // 删除tbody元素
    }

    // const kb1_body = document.getElementById('kkb1');
    // if (kb1_body !== null) {
    //     kb1_body.innerHTML = '';
    // }
}

function appendEmptyQueue(tbody) {
    // 创建表格内容
    // const tbody = document.createElement('tbody');
    // 显示队列为空的提示信息
    const emptyRow = document.createElement('tr');
    const emptyCell = document.createElement('td');
    emptyCell.textContent = '请求队列为空。';
    emptyRow.appendChild(emptyCell);
    tbody.appendChild(emptyRow);

    for (let i = 0; i < 4; i++) {
        const row = document.createElement('tr');
        const cell = document.createElement('td');
        cell.textContent = ' ';
        row.appendChild(cell);
        tbody.appendChild(row);
    }
    // return tbody;
}
