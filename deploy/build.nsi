

!include "MUI2.nsh"

!define VERSION "2.0.17"

Name "dsBudget"

OutFile "C:\Users\soichi\Desktop\dsbudget_${VERSION}.exe"

; Some default compiler settings (uncomment and change at will):
; SetCompress auto ; (can be off or force)
; SetDatablockOptimize on ; (can be off)
; CRCCheck on ; (can be off)
; AutoCloseWindow false ; (can be true for the window go away automatically at end)
; ShowInstDetails hide ; (can be show to have them shown, or nevershow to disable)
; SetDateSave off ; (can be on to have files restored to their orginal date)

InstallDir "$PROGRAMFILES\dsBudget"
InstallDirRegKey HKEY_LOCAL_MACHINE "SOFTWARE\dsBudget" ""

DirText "Select the directory to install dsBudget in:"

RequestExecutionLevel admin
Function .OnInit
 
UAC_Elevate:
    UAC::RunElevated 
    StrCmp 1223 $0 UAC_ElevationAborted ; UAC dialog aborted by user?
    StrCmp 0 $0 0 UAC_Err ; Error?
    StrCmp 1 $1 0 UAC_Success ;Are we the real deal or just the wrapper?
    Quit
 
UAC_Err:
    MessageBox mb_iconstop "Unable to elevate, error $0"
    Abort
 
UAC_ElevationAborted:
    # elevation was aborted, run as normal?
    MessageBox mb_iconstop "This installer requires admin access, aborting!"
    Abort
 
UAC_Success:
    StrCmp 1 $3 +4 ;Admin?
    StrCmp 3 $1 0 UAC_ElevationAborted ;Try again?
    MessageBox mb_iconstop "This installer requires admin access, try again"
    goto UAC_Elevate 
 
  ReadRegStr $R0 HKLM \
  "Software\Microsoft\Windows\CurrentVersion\Uninstall\dsBudget" \
  "UninstallString"
  StrCmp $R0 "" done
 
	; Make sure no dsbudget is running - this isn't perfect but...
	DetailPrint "Requesting to terminate dsBudget - if it's already running"
	NSISdl::download_quiet /TIMEOUT=100 http://127.0.0.1:16091/dsbudget/stop $TEMP/dsbudget.stop_request

  MessageBox MB_OKCANCEL|MB_ICONEXCLAMATION \
  "dsBudget is already installed. $\n$\nClick `OK` to remove the \
  previous version or `Cancel` to cancel this upgrade. \
  (Your document will be kept)" \
  IDOK uninst
  Abort
 
;Run the uninstaller
uninst:
  ClearErrors
  ExecWait '$R0 _?=$INSTDIR' ;Do not copy the uninstaller to a temp file
 
  IfErrors no_remove_uninstaller done
    ;You can either use Delete /REBOOTOK in the uninstaller or add some code
    ;here to remove the uninstaller. Use a registry key to check
    ;whether the user has chosen to uninstall. If you are using an uninstaller
    ;components page, make sure all sections are uninstalled.
  no_remove_uninstaller:
 
done:
 
 
FunctionEnd

Section "Normal" ; (default section)

	;check if we have java
	Call GetJRE
	Pop $R0
	
	StrCmp $R0 "" NoJava
	
	; main installation
	SetOutPath "$INSTDIR"
	File "build\dsbudget\dsbudget.jar"
	File "build\dsbudget\shortcut.ico"
	;File "build\dsbudget\dsbudget.conf"
	
	; install appdata
	CreateDirectory $APPDATA\dsBudget
	SetOutPath $APPDATA\dsBudget
	
	File /r "build\dsbudget\tomcat"
	
	;IfFileExists "$APPDATA\dsBudget\dsbudget.conf" AskConfOverwrite InstallConf
	;AskConfOverwrite:
	;	MessageBox MB_YESNO|MB_ICONQUESTION "Do you want to override your existing dsBudget configuration file (recommended)?" IDNO DoneConfInstall
	;InstallConf:	
	;	File "build\dsbudget\dsbudget.conf"
	;	Goto DoneConfInstall
	;DoneConfInstall:
	File "build\dsbudget\dsbudget.conf"
		
	IfFileExists "$APPDATA\dsBudget\BudgetDocument.xml" DoneDocInstall DocNotExists
	DocNotExists:
		DetailPrint "BudgetDocument.xml is not installed"
		IfFileExists "$INSTDIR\BudgetDocument.xml" CopyV16 TryOld
		CopyV16:
			messageBox MB_OK "We need to copy your exising BudgetDocument.xml to $APPDATA\dsBudget"
			CopyFiles $INSTDIR\BudgetDocument.xml $APPDATA\dsBudget\BudgetDocument.xml
			Goto DoneDocInstall
			
		TryOld:
			ReadRegStr $0 HKEY_CURRENT_USER "Software\SimpleD Software\SimpleD Budget\Settings" "PrevDoc"
			StrCmp $0 "" CopySample
			
			IfFileExists $0 CopyOld CopySample
			CopyOld:
				messageBox MB_OK "You have SimpleD Budget document in $0. Creating a copy for dsBudget at $APPDIR\dsBudget."
				DetailPrint "Copying SimpleD Budget doc from program files location"
				CopyFiles $0 $APPDIR/BudgetDocument.xml
				Goto DoneDocInstall
				
			CopySample:
				DetailPrint "Installing Sample Doc from $0"
				File "BudgetDocument.xml"
			
		;I've heard that windows 7 somehow leaves the document in read-only state.. let's see if this helps
		SetFileAttributes $INDSTDIR/BudgetDocument.xml FILE_ATTRIBUTE_NORMAL
	
	DoneDocInstall:
	
	; Create Start Menu shortcuts
	CreateDirectory $SMPROGRAMS\dsBudget
			
	CreateShortCut "$DESKTOP\dsBudget.lnk" "$R0" '-Ddivrep_invalidate_samepagekey -jar "$INSTDIR\dsbudget.jar"' '$INSTDIR\shortcut.ico'
	createShortCut "$SMPROGRAMS\dsBudget\Start dsBudget.lnk" "$R0" '-Ddivrep_invalidate_samepagekey -jar "$INSTDIR\dsbudget.jar"' '$INSTDIR\shortcut.ico'
	createShortCut "$INSTDIR\run.lnk" "$R0" '-Ddivrep_invalidate_samepagekey -jar "$INSTDIR\dsbudget.jar"'
	
	;run as admin (doesn't seem to do anyyhing)
	;WriteRegStr HKEY_LOCAL_MACHINE "Software\Microsoft\Windows NT\CurrentVersion\AppCompatFlags\layers" "$DESKTOP\dsBudget.lnk" "RUNASADMIN"
	
	
	createShortCut "$SMPROGRAMS\dsBudget\Uninstall dsBudget.lnk" "$INSTDIR\uninstall.exe"
	
	WriteRegStr HKEY_LOCAL_MACHINE "SOFTWARE\dsBudget" "" "$INSTDIR"
	WriteRegStr HKEY_LOCAL_MACHINE "Software\Microsoft\Windows\CurrentVersion\Uninstall\dsBudget" "DisplayName" "dsBudget (remove only)"
	WriteRegStr HKEY_LOCAL_MACHINE "Software\Microsoft\Windows\CurrentVersion\Uninstall\dsBudget" "UninstallString" '"$INSTDIR\uninstall.exe"'
	; write out uninstaller
	WriteUninstaller "$INSTDIR\uninstall.exe"

	;done of normal installation - define some special cases
	Goto Done
	
	NoJava:
	messageBox MB_OK "dsBudget requires Java to be installed on your machine. Please download it from http://java.com and install it first."
	
	Done:
	
SectionEnd ; end of default section

; begin uninstall settings/section
UninstallText "This will uninstall dsBudget from your system"

Section Uninstall
		
	RMDir /r "$INSTDIR"
	RMDir /r "$SMPROGRAMS\dsBudget"
	Delete "$DESKTOP\dsBudget.lnk"
	
	DeleteRegKey HKEY_LOCAL_MACHINE "SOFTWARE\dsBudget"
	DeleteRegKey HKEY_LOCAL_MACHINE "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\dsBudget"

SectionEnd ; end of uninstall section

Function GetJRE
;
;  returns the full path of a valid java.exe
;  looks in:
;  1 - .\jre directory (JRE Installed with application)
;  2 - JAVA_HOME environment variable
;  3 - the registry
;  4 - hopes it is in current dir or PATH
 
  Push $R0
  Push $R1
 
  ; use javaw.exe to avoid dosbox.
  ; use java.exe to keep stdout/stderr
  !define JAVAEXE "javaw.exe"
 
  ClearErrors
  StrCpy $R0 "$EXEDIR\jre\bin\${JAVAEXE}"
  IfFileExists $R0 JreFound  ;; 1) found it locally
  StrCpy $R0 ""
 
  ClearErrors
  ReadEnvStr $R0 "JAVA_HOME"
  StrCpy $R0 "$R0\bin\${JAVAEXE}"
  IfErrors 0 JreFound  ;; 2) found it in JAVA_HOME
 
  ClearErrors
  ReadRegStr $R1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
  ReadRegStr $R0 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$R1" "JavaHome"
  StrCpy $R0 "$R0\bin\${JAVAEXE}"
 
  IfErrors 0 JreFound  ;; 3) found it in the registry
  StrCpy $R0 "" ;; couldn't find it..
 
 JreFound:
  Pop $R1
  Exch $R0
FunctionEnd

Function .OnInstFailed
    UAC::Unload ;Must call unload!
FunctionEnd
 
Function .OnInstSuccess
    UAC::Unload ;Must call unload!
FunctionEnd