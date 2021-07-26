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
set PROPERTIES=-Dfile.encoding=gbk

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
