# badword
敏感词检查过滤服务


过滤模式：
POST http://localhost:8008/badword/filter
BODY: 放入要过滤的语句


检查模式：
POST http://localhost:8008/badword/check
BODY：放入要检查的语句


config 文件夹需要放在jar同目录下
