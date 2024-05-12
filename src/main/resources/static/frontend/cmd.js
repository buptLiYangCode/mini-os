// 必须加上window.onload = function() {} 才行，为了确保在操作DOM元素之前，DOM已经完全加载。
window.onload = function() {
    const cmdWindow = document.getElementById('cmd-window');
    const cmdInput = document.getElementById('cmd-input');

    // 处理用户输入命令
    function handleCommand(input) {
        // 在这里发送指令给后端，并处理返回结果
        displayOutput('>> ' + input);
        console.log(typeof (input));
        if (input === "cp") {
            send_CreateProcess();
        } else if (input === "exe") {
            send_ExecuteProcess();
        } else {
            send_cmd(input);
            // send2(input);
        }

    }
    
    // 执行进程
    async function send_cmd(input){
        var myHeaders = new Headers();
        myHeaders.append("User-Agent", "Apifox/1.0.0 (https://apifox.com)");

        var requestOptions = {
            method: 'POST',
            headers: myHeaders,
            redirect: 'follow'
        };
        // 必须链接成字符串发送，发body就接收不了，400报错，兼容后端处理逻辑
        url_file = "http://127.0.0.1:8080/mini-os/file-system/user-inst?inst=" + input;
        fetch(url_file, requestOptions)
            .then(response => response.json())
            .then(result => {
                console.log('cmd获取返回成功');
                console.log(result);
                const output_context = result.data;
                console.log(output_context);
                if (output_context !== null) {
                    const outputElement = document.createElement('div');
                    outputElement.textContent = output_context;
                    cmdWindow.appendChild(outputElement);
                }
                cmdWindow.scrollTop = cmdWindow.scrollHeight; // 滚动到底部
            })
            .catch(error => console.log('error', error));
    }


    // 显示输出结果
    function displayOutput(output) {
        const outputElement = document.createElement('div');
        outputElement.textContent = output;
        cmdWindow.appendChild(outputElement);
        console.log("output is: " + output);
        cmdWindow.scrollTop = cmdWindow.scrollHeight; // 滚动到底部
    }

    // 处理用户输入
    function handleInput() {
        const input = cmdInput.value.trim();
        if (input !== '') {
            handleCommand(input);
            cmdInput.value = ''; // 清空输入
        }
    }

    // 监听回车键
    cmdInput.addEventListener('keydown', function(event) {
        if (event.key === 'Enter') {
            console.log('监听到回车')
            handleInput();
        } else {
            console.log('监听到' + event.key);
        }
    });

    //-------------- -------------- -------------- -------------- -------------- -------------- -------------- ----------
    // 创建进程
    async function send_CreateProcess() {
        var myHeaders = new Headers();
        // myHeaders.append("User-Agent", "Apifox/1.0.0 (https://apifox.com)");
        myHeaders.append("Content-Type", "application/json");

        var raw = JSON.stringify({
            "processName": "huahuo000",
            "instructions": [
                "M 2",
                "C 500",
                "A 0",
                "D K1 2000",
                "D K3 2000",
                "C 500",
                "D K1 1000",
                "D K3 2000",
                "A 1",
                "A 0",
                "C 500",
                "D K3 600",
                "Q"
            ]
        });

        var requestOptions = {
            method: 'POST',
            headers: myHeaders,
            body: raw,
            redirect: 'follow'
        };

        fetch("http://127.0.0.1:8080/mini-os/process/create", requestOptions)
            .then(response => response.text())
            .then(result => {
                console.log(result);
                displayOutput('Process created successfully!');
            })
            .catch(error => console.log('error', error));
    }
    
    // 执行进程
    async function send_ExecuteProcess() {
        var myHeaders = new Headers();
        // myHeaders.append("User-Agent", "Apifox/1.0.0 (https://apifox.com)");
        myHeaders.append("Content-Type", "application/json");

        var raw = JSON.stringify({
            "processName": "huahuo000"
        });

        var requestOptions = {
            method: 'POST',
            headers: myHeaders,
            body: raw,
            redirect: 'follow'
        };

        fetch("http://127.0.0.1:8080/mini-os/process/execute", requestOptions)
            .then(response => response.json())
            .then(result => {
                console.log(result);
                const r = result.toString();
                displayOutput('Executing !');
            })
            .catch(error => console.log('error', error));
    }
}
