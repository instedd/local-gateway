OutFile "build\LocalGateway.msi"
Name "Nuntium Local Gateway"
InstallDir "$ProgramFiles\InSTEDD\Nuntium Local Gateway"

Page directory
Page instfiles
UninstPage uninstConfirm
UninstPage instfiles

Section
  SetOutPath "$INSTDIR"
  WriteUninstaller "$INSTDIR\Uninstall.exe"
  File /oname=NuntiumLocalGateway.jar build\NuntiumLocalGateway.jar
  File /oname=rxtxSerial.dll lib\rxtxSerial.dll
  File /oname=NuntiumLocalGateway.ico build.lib\Icons\favico.ico
  File /oname=NuntiumLocalGateway.png src\org\instedd\mobilegw\ui\icon_72.png
  CreateDirectory "$SMPROGRAMS\InSTEDD\Nuntium Local Gateway"
  CreateShortCut "$SMPROGRAMS\InSTEDD\Nuntium Local Gateway\Nuntium Local Gateway.lnk" "$INSTDIR\NuntiumLocalGateway.jar" "" "$INSTDIR\NuntiumLocalGateway.ico"
  CreateShortCut "$SMPROGRAMS\InSTEDD\Nuntium Local Gateway\Uninstall.lnk" "$INSTDIR\Uninstall.exe"
SectionEnd

Section uninstall
  RMDir /r "$INSTDIR"
  RMDir /r "$SMPROGRAMS\InSTEDD\Nuntium Local Gateway"
SectionEnd