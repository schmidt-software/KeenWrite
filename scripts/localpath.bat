@echo off
set "OWNPATH=%~dp0"
set "PLATFORM=mswin"

if defined ProgramFiles(x86)                        set "PLATFORM=win64"
if "%PROCESSOR_ARCHITECTURE%"=="AMD64"              set "PLATFORM=win64"
if exist "%OWNPATH%tex\texmf-mswin\bin\context.exe" set "PLATFORM=mswin"
if exist "%OWNPATH%tex\texmf-win64\bin\context.exe" set "PLATFORM=win64"

set "TeXPath=%OWNPATH%tex\texmf-%PLATFORM%\bin"

echo %PATH% | findstr "texmf-%PLATFORM%" > nul

rem Only update the PATH if not previously updated
if ERRORLEVEL 1 (
  setlocal enabledelayedexpansion
  set "Exists=false"
  set "Key=HKCU\Environment"
  
  for /F "USEBACKQ tokens=2*" %%A in (`reg query %%Key%% /v PATH 2^>nul`) do (
    if not "%%~B" == "" (
      set "Exists=true"

      rem Preserve the existing PATH
      echo %%B > currpath.txt

      rem Change the PATH environment variable
      setx PATH "%%B;%TeXPath%"
    )
  )

  rem The user-defined PATH does not exist, create it
  if "!Exists!" == "false" (
    rem Change the user PATH environment variable
    setx PATH "%TeXPath%"
  )

  endlocal
  
  rem Update the current session
  set "PATH=%PATH%;%TeXPath%"
)

