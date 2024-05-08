document.addEventListener('DOMContentLoaded', function() {
    // 创建矩阵
    const matrixContainer = document.getElementById('mem_matrix');
    const matrix = [];

    for (let row = 0; row < 8; row++) {
        matrix[row] = [];
        for (let col = 0; col < 8; col++) {
            const rectangle = document.createElement('div');
            // const rectangleText = document.createElement('span');
            // rectangleText.textContent = '';
            rectangle.textContent = ' ';
            rectangle.classList = ['mem_block'];
            rectangle.style.justifyContent = 'center';
            rectangle.style.alignItems = 'center';
            // rectangle.appendChild(rectangleText);
            matrixContainer.appendChild(rectangle);
            matrix[row][col] = rectangle;
        }
        matrixContainer.appendChild(document.createElement('br'));
    }

    // 为矩形内的文本应用样式
    const rectangles = document.querySelectorAll('.mem_block');
    rectangles.forEach(rectangle => {
        rectangle.style.position = 'relative';
        rectangle.style.display = 'inline-block';
        // rectangle.style.width = '100px'; // 根据需要调整宽度
        rectangle.style.height = '44px'; // 根据需要调整高度
        rectangle.style.textAlign = 'center';
        rectangle.style.lineHeight = rectangle.style.height;
        rectangle.style.verticalAlign = 'middle';
    });
    
    const url_mem = 'http://localhost:8080/mini-os/memory/query-all';
    
    const color_process = [
        { color: 'white', id: 1 },
    ];

    function getRandomColor() {
        var letters = '0123456789ABCDEF';
        var color = '#';
        for (var i = 0; i < 6; i++) {
            color += letters[Math.floor(Math.random() * 16)];
        }
        // 添加透明度，这里是 50%
        return color + '80'; // '80' 表示透明度，16进制，0x80 = 128
        // return color;
    }

    var same_data_time = 0;
    var last_mem_matrix;
    // 更新矩阵的颜色和tooltip
    function updateMatrix(data) {
        console.log('内存块获取成功');
        const memPageInfoList = data.data.list;
        if (last_mem_matrix !== memPageInfoList) {
            var block_no = 0;
            memPageInfoList.forEach(mem_block => {
                var row = Math.floor(block_no / 8);
                var col = Math.floor(block_no % 8);
                // if (mem_block.pid !== -1) {
                    const rectangle = matrix[row][col];
                    // console.log(row,col,matrix);
                    let pid_exist = false;
                    if (mem_block.pid !== -1) {
                        for (let i = 0; i < color_process.length; i++) {
                            if (color_process[i].id === mem_block.pid) {
                                rectangle.style.backgroundColor = color_process[i].color;
                                pid_exist = true;
                                break;
                            }
                        }
                        if (!pid_exist) {
                            color_process.push({color: getRandomColor(), id: mem_block.pid});
                            const logT = color_process[length].toString();
                            console.log(logT);
                            rectangle.style.backgroundColor = color_process[length].color;
                        }
                        rectangle.innerHTML = '';
                        // const rectangleText = document.createElement('span');
                        if (block_no < 9) {
                            rectangle.textContent = `${mem_block.pid}`;
                        } else if (mem_block.vpn !== -1){
                            rectangle.textContent = `${mem_block.vpn}`;
                        }
                        // rectangle.appendChild(rectangleText);
                    } else if (mem_block.pid === -1){
                        rectangle.style.backgroundColor = 'rgba(211, 211, 211, 0.4)';
                        rectangle.textContent = ' ';
                    }
                    
                    

                    // 创建提示框元素
                    const tooltip = document.createElement('div');
                    // tooltip.textContent = 'Your tooltip content'; // 设置提示框内容
                    tooltip.classList = ['tooltip']; // 添加样式类

                    // 将提示框元素附加到矩形元素上
                    rectangle.appendChild(tooltip);

                    rectangle.addEventListener('mouseenter', () => {
                        if (mem_block.pid !== -1) {
                            tooltip.textContent = `内存块号: ${row * 8 + col}\nPID: ${mem_block.pid}\n虚拟页号: ${mem_block.vpn}`; // 小正方形的坐标信息
                        } else {
                            tooltip.textContent = `内存块号: ${row * 8 + col}\nPID: null \n虚拟页号: null`; // 小正方形的坐标信息
                        }
                        tooltip.style.left = `${0 * rectangle.offsetLeft}px`;
                        tooltip.style.top = `${0 * (rectangle.offsetTop + rectangle.offsetHeight)}px`;
                        tooltip.style.display = 'block';
                        tooltip.style.width = '140px';
                        tooltip.style.zIndex = '9999'; // 或者更大的值，确保在其他元素上层显示
                    });
                    rectangle.addEventListener('mouseleave', () => {
                        tooltip.style.display = 'none';
                    });
                // } else if (mem_block.pid === -1) {
                //    
                // }
                block_no++;
            })
            last_mem_matrix = memPageInfoList;
            same_data_time = 0;
        } else if (same_data_time > 10) {
            for (let row = 0; row < 8; row++) {
                for (let col = 0; col < 8; col++) {
                    matrix[row][col].backgroundColor = 'rgba(220, 222, 212, 0.4)';
                }
            }
            same_data_time = 0;
        } else {
            same_data_time++;
        }
        
    }

    function fetchData() {
        fetch(url_mem)
            .then(response => response.json())
            .then(data => {
                updateMatrix(data);
            })
            .catch(error => console.error(error));
    }

    fetchData();
    setInterval(fetchData, 100);
});
