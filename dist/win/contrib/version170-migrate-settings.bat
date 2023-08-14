@echo off
:: see comments in file ./version170-migrate-settings.ps1

cd %~dp0
powershell -NoLogo -NoProfile -NonInteractive -ExecutionPolicy AllSigned -Command .\version170-migrate-settings.ps1