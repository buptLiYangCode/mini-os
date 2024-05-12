const url_file_tree = 'http://localhost:8080/mini-os/file-system/file-tree';

const requestOptions = {
    method: 'GET',
    redirect: 'follow'
};

// 发送请求并处理返回的数据
function fetchData() {
    // fetch不能加第二个参数，requestOptions，加上就发不出去请求,兼容后端处理逻辑
    // 使用 fetch 函数发送一个 GET 请求到指定的 url。
    fetch(url_file_tree)
        // 使用 then 方法处理服务器返回的响应对象。
        // 调用 response.json() 方法，将响应解析为 JSON 格式的数据。
        .then(response => response.json())
        // 使用 then 方法处理解析后的 JSON 数据.将获取到的 JSON 数据存储在名为 data 的变量中，并执行后续的操作
        .then(result => {
            console.log('文件树获取成功');
            const tree = result.data;
            console.log(tree);
            document.getElementById('fs-tree').innerHTML = tree;
        })
        .catch(error => console.error(error));
}

// 第一次调用 fetchData 函数，用于页面加载时立即获取数据。
fetchData();
// 使用 setInterval 函数定时调用 fetchData 函数，间隔时间为 24/1000 毫秒
setInterval(fetchData, 100);
