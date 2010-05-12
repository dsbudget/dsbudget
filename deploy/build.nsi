

!include "MUI2.nsh"

!define VERSION "2.0.18"

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
	
	; main installation
	SetOutPath "$INSTDIR"
	File "build\dsbudget.exe"
	;File "build\win-installer\shortcut.ico"
	
	; install appdata
	CreateDirectory $APPDATA\dsBudget
	SetOutPath $APPDATA\dsBudget
	File /r "build\win-installer\tomcat"
	File "build\win-installer\dsbudget.conf"

	; install user conf if it doesn't exist yet
	IfFileExists "$APPDATA\dsBudget\dsbudget.user.conf" DoneUserConfInstall UserConfNotExists
	UserConfNotExists:
		File "build\win-installer\dsbudget.user.conf"
	DoneUserConfInstall:

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
				messageBox MB_OK "You have SimpleD Budget document in $0. Creating a copy for dsBudget at $APPDATA\dsBudget."
				DetailPrint "Copying SimpleD Budget doc from program files location"
				CopyFiles $0 $APPDATA/BudgetDocument.xml
				Goto DoneDocInstall
				
			CopySample:
				DetailPrint "Installing Sample Doc from $0"
				File "BudgetDocument.xml"
			
		;I've heard that windows 7 somehow leaves the document in read-only state.. let's see if this helps
		SetFileAttributes $INSTDIR/BudgetDocument.xml FILE_ATTRIBUTE_NORMAL
	
	DoneDocInstall:
	
	; Create Start Menu shortcuts
	CreateDirectory $SMPROGRAMS\dsBudget
			
	CreateShortCut "$DESKTOP\dsBudget.lnk" "$INSTDIR\dsbudget.exe"
	createShortCut "$SMPROGRAMS\dsBudget\Start dsBudget.lnk" "$INSTDIR\dsbudget.exe"
		
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
	RMDir /r "$APPDATA\dsBudget\tomcat"
	Delete "$DESKTOP\dsBudget.lnk"
	
	DeleteRegKey HKEY_LOCAL_MACHINE "SOFTWARE\dsBudget"
	DeleteRegKey HKEY_LOCAL_MACHINE "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\dsBudget"

SectionEnd ; end of uninstall section

Function .OnInstFailed
    UAC::Unload ;Must call unload!
FunctionEnd
 
Function .OnInstSuccess
    UAC::Unload ;Must call unload!
FunctionEnd