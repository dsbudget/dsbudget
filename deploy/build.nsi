
RequestExecutionLevel admin

!include "MUI2.nsh"

Name "dsBudget"
OutFile "/home/soichih/Desktop/dsbudget_2.0.X.exe"

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

;Request application privileges for Windows Vista
RequestExecutionLevel user

Section "Normal" ; (default section)

	;check if we have java
	Call GetJRE
	Pop $R0
	
	StrCmp $R0 "" NoJava
	
	; main installation
	SetOutPath "$INSTDIR"
	File /r "build\dsbudget\tomcat"
	File "build\dsbudget\dsbudget.jar"
	File "build\dsbudget\shortcut.ico"
	File "build\dsbudget\dsbudget.conf"
		
	IfFileExists "$INSTDIR/BudgetDocument.xml" DoneDocInstall DocNotExists
	DocNotExists:
		DetailPrint "BudgetDocument.xml is not installed"
		
		;if we don't have BudgetDocument.xml in the install dir yet, and SimpleD Budget doc exists, copy it
		ReadRegStr $0 HKEY_CURRENT_USER "Software\SimpleD Software\SimpleD Budget\Settings" "PrevDoc"
		DetailPrint "SimpleD Budget PrevDoc is set at $0 - trying to copy"
		StrCmp $0 "" CopySample
		
		IfFileExists $0 CopyOld CopySample
	CopyOld:
		messageBox MB_OK "You have SimpleD Budget document in $0. Creating a copy for dsBudget at $INSTDIR."
		DetailPrint "Copying SimpleD Budget doc"
		CopyFiles $0 $INSTDIR/BudgetDocument.xml
		Goto DoneDocInstall
		
	CopySample:
		DetailPrint "Installing Sample Doc"
		File "BudgetDocument.xml"
		
	;I've heard that windows 7 somehow leaves the document in read-only state.. let's see if this helps
	SetFileAttributes $INDSTDIR/BudgetDocument.xml FILE_ATTRIBUTE_NORMAL
	
	DoneDocInstall:
	
	; Create Start Menu shortcuts
	CreateDirectory $SMPROGRAMS\dsBudget
	
	CreateShortCut "$DESKTOP\dsBudget.lnk" "$R0" '-jar dsbudget.jar' '$INSTDIR\shortcut.ico'
	createShortCut "$SMPROGRAMS\dsBudget\Start dsBudget.lnk" "$R0" '-jar dsbudget.jar' '$INSTDIR\shortcut.ico'

	createShortCut "$INSTDIR\run.lnk" "$R0" '-jar dsbudget.jar'
	
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
	
	RMDir /r "$INSTDIR\tomcat"
	Delete "$INSTDIR\dsbudget.jar"
	Delete "$INSTDIR\shortcut.ico"
	Delete "$INSTDIR\dsbudget.conf"
	Delete "$INSTDIR\uninstall.exe"
	Delete "$INSTDIR\run.lnk"

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
