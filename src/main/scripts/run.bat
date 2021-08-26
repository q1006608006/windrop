@echo off

set allparam=

:param
set str=%1
if "%str%"=="" (
    goto end
)
set allparam=%allparam% %str%
shift /0
goto param
:end

set path=%path%;.;
set app_jar=${project.build.finalName}.jar

set home_path=%cd%

:: you can add or set your running parameter in here
set PARAM=%allparam%

:: you can add your jvm-properties in here
:: 程序使用gbk编码防止显示乱码
set PROPERTIES=-Dfile.encoding=gbk

:: 若需要指定网络接口删除下行的注释符，并将'lan1,wifi2'替换为你的网络适配器描述名词，如："Realtek PCIe Controller",可以指定多个，用逗号隔开
:: set PROPERTIES=%PROPERTIES% -Dwindrop.networkInterfaces="lan1,wifi2"

set conf_path=%home_path%/conf
set lib_path=%home_path%/libs
set loader_path=%conf_path%,%lib_path%

:: if you has your jar-libs,you can add them in here
set extra_path=

if defined extra_path  (
    set loader_path=%loader_path%,%extra_path%
)


set CMD=javaw %PROPERTIES% -Dloader.path=%loader_path% -jar %lib_path%/%app_jar% %PARAM%

echo %CMD%

start %CMD%

:: pause
