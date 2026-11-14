package com.zakshub.nexus.data

import com.zakshub.nexus.domain.model.CommandParameterBreakdown
import com.zakshub.nexus.domain.model.CommandRecipe
import com.zakshub.nexus.domain.model.DevicePlatform
import com.zakshub.nexus.domain.model.DeviceProfile
import com.zakshub.nexus.domain.model.EnvironmentProfile
import com.zakshub.nexus.domain.model.ExecutionEnvironmentType
import com.zakshub.nexus.domain.model.GuidedWorkflow
import com.zakshub.nexus.domain.model.GuidedWorkflowStep
import com.zakshub.nexus.domain.model.ImpactLevel
import com.zakshub.nexus.domain.model.TroubleshootingCase
import com.zakshub.nexus.domain.model.VerificationStatus

/**
 * Curated starter catalog for the owner's actual device ecosystem:
 * - Google Pixel 7 Pro (12 GB RAM, Android, Termux Host, Debian proot)
 * - Windows 11 Laptop (PowerShell & OpenSSH client)
 */
object StarterKnowledgeCatalog {

    val devices: List<DeviceProfile> = listOf(
        DeviceProfile(
            id = "device-pixel7pro",
            name = "Google Pixel 7 Pro",
            platform = DevicePlatform.ANDROID,
            hardwareSummary = "12 GB RAM · Android · Primary Mobile Host",
            roleDescription = "Runs NEXUS APK, Termux Host Shell (SSH Server on port 8022), and Debian Linux via proot.",
            isVerified = true
        ),
        DeviceProfile(
            id = "device-win11",
            name = "Windows 11 Laptop",
            platform = DevicePlatform.WINDOWS,
            hardwareSummary = "Windows 11 · PowerShell & OpenSSH Client",
            roleDescription = "Primary design & workstation laptop used to connect into the Pixel 7 Pro over SSH.",
            isVerified = true
        )
    )

    val environments: List<EnvironmentProfile> = listOf(
        EnvironmentProfile(
            id = "env-termux-host",
            deviceId = "device-pixel7pro",
            deviceName = "Google Pixel 7 Pro",
            type = ExecutionEnvironmentType.TERMUX_HOST,
            shellSummary = "Termux Host Shell (`pkg` package manager · Port 8022 SSH Server)",
            filesystemBoundaryNote = "Lives in `/data/data/com.termux/files/usr`. This is where you land first when connecting over SSH from Windows.",
            commonPitfallWarning = "Do NOT run Debian `apt` commands here. Use `pkg` for Termux tools (`sshd`, `termux-wake-lock`), or enter Debian first."
        ),
        EnvironmentProfile(
            id = "env-debian-proot",
            deviceId = "device-pixel7pro",
            deviceName = "Google Pixel 7 Pro",
            type = ExecutionEnvironmentType.DEBIAN_PROOT,
            shellSummary = "Debian Linux inside Termux (`proot` · `apt` package manager)",
            filesystemBoundaryNote = "Simulated Debian root filesystem (`/`) running inside Termux via `proot-distro login debian`.",
            commonPitfallWarning = "Do NOT run `termux-wake-lock` or start Termux `sshd` from inside Debian. Exit back to Termux Host first (`exit`)."
        ),
        EnvironmentProfile(
            id = "env-win11-ps",
            deviceId = "device-win11",
            deviceName = "Windows 11 Laptop",
            type = ExecutionEnvironmentType.WINDOWS_POWERSHELL,
            shellSummary = "Windows PowerShell / Terminal (`ssh`, `git`, `gh`)",
            filesystemBoundaryNote = "Your laptop's native Windows environment (`C:\\Users\\...`).",
            commonPitfallWarning = "Always include `-p 8022` when connecting with `ssh` to your phone, because Termux does not use port 22."
        )
    )

    val recipes: List<CommandRecipe> = listOf(
        CommandRecipe(
            id = "recipe-termux-start-sshd",
            title = "Keep Termux Awake & Start SSH Server",
            targetDeviceName = "Google Pixel 7 Pro",
            targetPlatform = DevicePlatform.ANDROID,
            environmentType = ExecutionEnvironmentType.TERMUX_HOST,
            purposePlainEnglish = "Prevents Android from putting Termux to sleep when the phone screen turns off, and starts the Termux SSH server so your Windows 11 laptop can connect.",
            commandSyntax = "termux-wake-lock && sshd",
            parameters = listOf(
                CommandParameterBreakdown(
                    token = "termux-wake-lock",
                    meaningPlainEnglish = "Holds an Android wake-lock so Wi-Fi and terminal sessions stay alive when the screen locks."
                ),
                CommandParameterBreakdown(
                    token = "&&",
                    meaningPlainEnglish = "Runs the second command only if the first command succeeded."
                ),
                CommandParameterBreakdown(
                    token = "sshd",
                    meaningPlainEnglish = "Starts the OpenSSH server inside Termux listening on port 8022."
                )
            ),
            expectedOutputPlainEnglish = "No text error is printed, and a persistent 'Termux — Wake lock held' notification appears on your phone.",
            impactLevel = ImpactLevel.CONFIG_CHANGE,
            verificationStatus = VerificationStatus.VERIFIED,
            rollbackOrUndoHint = "To stop the SSH server later, run `pkill sshd && termux-wake-unlock` in Termux.",
            isPinnedDaily = true
        ),
        CommandRecipe(
            id = "recipe-termux-find-ip",
            title = "Find Phone Wi-Fi IP Address & Current Username",
            targetDeviceName = "Google Pixel 7 Pro",
            targetPlatform = DevicePlatform.ANDROID,
            environmentType = ExecutionEnvironmentType.TERMUX_HOST,
            purposePlainEnglish = "Shows your Pixel 7 Pro's current local Wi-Fi IP address (such as 192.168.1.x) so you know what address to type on your Windows 11 laptop.",
            commandSyntax = "whoami && ip -4 addr show wlan0 | grep inet",
            parameters = listOf(
                CommandParameterBreakdown(
                    token = "whoami",
                    meaningPlainEnglish = "Prints your Termux username (for example, u0_a245)."
                ),
                CommandParameterBreakdown(
                    token = "ip -4 addr show wlan0",
                    meaningPlainEnglish = "Checks the IPv4 network address of your phone's Wi-Fi connection (wlan0)."
                ),
                CommandParameterBreakdown(
                    token = "| grep inet",
                    meaningPlainEnglish = "Filters the output to only show the line containing the IP address."
                )
            ),
            expectedOutputPlainEnglish = "Shows your username on line 1, and `inet 192.168.x.x/24` on line 2 (use the `192.168.x.x` part on Windows).",
            impactLevel = ImpactLevel.READ_ONLY,
            verificationStatus = VerificationStatus.VERIFIED,
            isPinnedDaily = true
        ),
        CommandRecipe(
            id = "recipe-win11-ssh-connect",
            title = "Connect from Windows 11 to Phone Termux over SSH",
            targetDeviceName = "Windows 11 Laptop",
            targetPlatform = DevicePlatform.WINDOWS,
            environmentType = ExecutionEnvironmentType.WINDOWS_POWERSHELL,
            purposePlainEnglish = "Opens a secure terminal connection from your Windows 11 laptop into Termux on your Pixel 7 Pro using port 8022.",
            commandSyntax = "ssh -p 8022 <PHONE_WIFI_IP>",
            parameters = listOf(
                CommandParameterBreakdown(
                    token = "ssh",
                    meaningPlainEnglish = "Launches the Windows OpenSSH client."
                ),
                CommandParameterBreakdown(
                    token = "-p 8022",
                    meaningPlainEnglish = "Connects to Termux's SSH port (8022) instead of the standard port 22."
                ),
                CommandParameterBreakdown(
                    token = "<PHONE_WIFI_IP>",
                    meaningPlainEnglish = "Replace with the 192.168.x.x IP address shown by your phone."
                )
            ),
            expectedOutputPlainEnglish = "Your Windows PowerShell prompt changes to the Termux welcome banner and `~ \$` prompt.",
            impactLevel = ImpactLevel.READ_ONLY,
            verificationStatus = VerificationStatus.VERIFIED,
            isPinnedDaily = true
        ),
        CommandRecipe(
            id = "recipe-termux-enter-debian",
            title = "Enter Debian Linux (proot) from Termux Host",
            targetDeviceName = "Google Pixel 7 Pro",
            targetPlatform = DevicePlatform.ANDROID,
            environmentType = ExecutionEnvironmentType.TERMUX_HOST,
            purposePlainEnglish = "Switches your session from the outer Termux Host shell into your Debian Linux (proot) environment.",
            commandSyntax = "proot-distro login debian",
            parameters = listOf(
                CommandParameterBreakdown(
                    token = "proot-distro login debian",
                    meaningPlainEnglish = "Starts an interactive Debian Linux shell inside Termux using proot."
                )
            ),
            expectedOutputPlainEnglish = "Your prompt changes from `~ \$` to `root@localhost:~#`, confirming you are now inside Debian.",
            impactLevel = ImpactLevel.READ_ONLY,
            verificationStatus = VerificationStatus.VERIFIED,
            rollbackOrUndoHint = "Type `exit` at any time to leave Debian and return to the Termux Host shell.",
            isPinnedDaily = true
        ),
        CommandRecipe(
            id = "recipe-debian-verify-env",
            title = "Confirm You Are Inside Debian (Not Raw Termux)",
            targetDeviceName = "Google Pixel 7 Pro",
            targetPlatform = DevicePlatform.ANDROID,
            environmentType = ExecutionEnvironmentType.DEBIAN_PROOT,
            purposePlainEnglish = "Safely checks which Linux system you are currently inside before you install packages or run Debian tools.",
            commandSyntax = "cat /etc/os-release && df -h /",
            parameters = listOf(
                CommandParameterBreakdown(
                    token = "cat /etc/os-release",
                    meaningPlainEnglish = "Prints the Linux distribution name and version (should say Debian GNU/Linux)."
                ),
                CommandParameterBreakdown(
                    token = "df -h /",
                    meaningPlainEnglish = "Shows available storage space in human-readable gigabytes (GB)."
                )
            ),
            expectedOutputPlainEnglish = "Displays `PRETTY_NAME=\"Debian GNU/Linux ...\"` and your free disk space in GB.",
            impactLevel = ImpactLevel.READ_ONLY,
            verificationStatus = VerificationStatus.VERIFIED,
            isPinnedDaily = false
        ),
        CommandRecipe(
            id = "recipe-python-local-server",
            title = "Start Local Python Web Server on Port 8080",
            targetDeviceName = "Google Pixel 7 Pro",
            targetPlatform = DevicePlatform.ANDROID,
            environmentType = ExecutionEnvironmentType.TERMUX_HOST,
            purposePlainEnglish = "Runs a lightweight local web server from your current folder so you can preview HTML designs or download files on your Windows 11 laptop browser at http://<PHONE_IP>:8080.",
            commandSyntax = "python3 -m http.server 8080",
            parameters = listOf(
                CommandParameterBreakdown(
                    token = "python3",
                    meaningPlainEnglish = "Runs the Python interpreter installed in Termux."
                ),
                CommandParameterBreakdown(
                    token = "-m http.server 8080",
                    meaningPlainEnglish = "Starts a simple HTTP web server serving files on port 8080."
                )
            ),
            expectedOutputPlainEnglish = "Prints `Serving HTTP on 0.0.0.0 port 8080 ...`. Press Ctrl+C in Termux when done.",
            impactLevel = ImpactLevel.READ_ONLY,
            verificationStatus = VerificationStatus.VERIFIED,
            isPinnedDaily = true
        ),
        CommandRecipe(
            id = "recipe-termux-storage-clean",
            title = "Clean Package Cache to Free Up Phone Storage",
            targetDeviceName = "Google Pixel 7 Pro",
            targetPlatform = DevicePlatform.ANDROID,
            environmentType = ExecutionEnvironmentType.TERMUX_HOST,
            purposePlainEnglish = "Deletes old downloaded installer archives (.deb files) in Termux to reclaim storage space on your Pixel 7 Pro.",
            commandSyntax = "pkg clean && termux-wake-unlock",
            parameters = listOf(
                CommandParameterBreakdown(
                    token = "pkg clean",
                    meaningPlainEnglish = "Cleans cached installation packages from Termux cache."
                ),
                CommandParameterBreakdown(
                    token = "termux-wake-unlock",
                    meaningPlainEnglish = "Releases the Android wake lock if you are done working."
                )
            ),
            expectedOutputPlainEnglish = "Frees up internal storage space safely without removing any installed tools.",
            impactLevel = ImpactLevel.CONFIG_CHANGE,
            verificationStatus = VerificationStatus.VERIFIED,
            isPinnedDaily = false
        ),
        CommandRecipe(
            id = "recipe-git-safe-sync",
            title = "Check Git Working Tree & Fast-Forward Pull",
            targetDeviceName = "Google Pixel 7 Pro",
            targetPlatform = DevicePlatform.ANDROID,
            environmentType = ExecutionEnvironmentType.TERMUX_HOST,
            purposePlainEnglish = "Checks whether you have any uncommitted design files or code edits before safely updating your repository without messy merge conflicts.",
            commandSyntax = "git status -s && git pull --ff-only",
            parameters = listOf(
                CommandParameterBreakdown(
                    token = "git status -s",
                    meaningPlainEnglish = "Shows short summary of modified or untracked files."
                ),
                CommandParameterBreakdown(
                    token = "git pull --ff-only",
                    meaningPlainEnglish = "Pulls latest commits only if it can be fast-forwarded cleanly without creating a merge commit."
                )
            ),
            expectedOutputPlainEnglish = "Shows current changed files and updates branch to the latest commit.",
            impactLevel = ImpactLevel.CONFIG_CHANGE,
            verificationStatus = VerificationStatus.VERIFIED,
            isPinnedDaily = true
        ),
        CommandRecipe(
            id = "recipe-phone-ram-check",
            title = "Check Active RAM & Memory Usage on Pixel 7 Pro",
            targetDeviceName = "Google Pixel 7 Pro",
            targetPlatform = DevicePlatform.ANDROID,
            environmentType = ExecutionEnvironmentType.TERMUX_HOST,
            purposePlainEnglish = "Shows how much of your Pixel 7 Pro's 12 GB RAM is currently free, used, or cached by background processes.",
            commandSyntax = "free -h",
            parameters = listOf(
                CommandParameterBreakdown(
                    token = "free -h",
                    meaningPlainEnglish = "Displays memory statistics in human-readable gigabytes (GB)."
                )
            ),
            expectedOutputPlainEnglish = "Displays total memory (~11Gi), used memory, and available memory in gigabytes.",
            impactLevel = ImpactLevel.READ_ONLY,
            verificationStatus = VerificationStatus.VERIFIED,
            isPinnedDaily = false
        ),
        CommandRecipe(
            id = "recipe-termux-setup-storage",
            title = "Grant Phone Storage Access to Termux",
            targetDeviceName = "Google Pixel 7 Pro",
            targetPlatform = DevicePlatform.ANDROID,
            environmentType = ExecutionEnvironmentType.TERMUX_HOST,
            purposePlainEnglish = "Creates symlinks in `~/storage` to your phone's internal storage, Downloads, DCIM, and Movies folders.",
            commandSyntax = "termux-setup-storage",
            parameters = listOf(
                CommandParameterBreakdown(
                    token = "termux-setup-storage",
                    meaningPlainEnglish = "Requests Android storage permission and creates the `~/storage` directory links."
                )
            ),
            expectedOutputPlainEnglish = "Android prompts 'Allow Termux to access photos and files?'. Tap Allow.",
            impactLevel = ImpactLevel.CONFIG_CHANGE,
            verificationStatus = VerificationStatus.VERIFIED,
            isPinnedDaily = false
        ),
        CommandRecipe(
            id = "recipe-termux-goto-downloads",
            title = "Explore Phone Downloads Folder in Termux",
            targetDeviceName = "Google Pixel 7 Pro",
            targetPlatform = DevicePlatform.ANDROID,
            environmentType = ExecutionEnvironmentType.TERMUX_HOST,
            purposePlainEnglish = "Navigates into your Pixel 7 Pro's Downloads folder directly from the Termux shell so you can see downloaded files.",
            commandSyntax = "cd ~/storage/downloads && pwd && ls -lh",
            parameters = listOf(
                CommandParameterBreakdown(
                    token = "cd ~/storage/downloads",
                    meaningPlainEnglish = "Switches active directory into phone Downloads."
                ),
                CommandParameterBreakdown(
                    token = "pwd",
                    meaningPlainEnglish = "Prints working directory path to confirm location."
                ),
                CommandParameterBreakdown(
                    token = "ls -lh",
                    meaningPlainEnglish = "Lists all files with readable sizes (KB/MB)."
                )
            ),
            expectedOutputPlainEnglish = "Prints `/data/data/com.termux/files/home/storage/downloads` followed by your downloaded files.",
            impactLevel = ImpactLevel.READ_ONLY,
            verificationStatus = VerificationStatus.VERIFIED,
            isPinnedDaily = true
        ),
        CommandRecipe(
            id = "recipe-debian-explore-root",
            title = "Explore Debian Linux Root Directory",
            targetDeviceName = "Google Pixel 7 Pro",
            targetPlatform = DevicePlatform.ANDROID,
            environmentType = ExecutionEnvironmentType.DEBIAN_PROOT,
            purposePlainEnglish = "Navigates into `/root` inside Debian PRoot and lists all project files and hidden configuration files.",
            commandSyntax = "cd /root && pwd && ls -la",
            parameters = listOf(
                CommandParameterBreakdown(
                    token = "cd /root",
                    meaningPlainEnglish = "Changes directory into Debian's root user home folder."
                ),
                CommandParameterBreakdown(
                    token = "ls -la",
                    meaningPlainEnglish = "Lists all files including hidden dotfiles (.bashrc, .git, etc.)."
                )
            ),
            expectedOutputPlainEnglish = "Shows `/root` followed by the contents of your Linux workspace.",
            impactLevel = ImpactLevel.READ_ONLY,
            verificationStatus = VerificationStatus.VERIFIED,
            isPinnedDaily = true
        ),
        CommandRecipe(
            id = "recipe-debian-exit-to-termux",
            title = "Safely Exit Debian and Return to Termux",
            targetDeviceName = "Google Pixel 7 Pro",
            targetPlatform = DevicePlatform.ANDROID,
            environmentType = ExecutionEnvironmentType.DEBIAN_PROOT,
            purposePlainEnglish = "Closes the Debian proot container session and returns you to the native Termux host shell.",
            commandSyntax = "exit",
            parameters = listOf(
                CommandParameterBreakdown(
                    token = "exit",
                    meaningPlainEnglish = "Terminates the current shell process and returns to parent."
                )
            ),
            expectedOutputPlainEnglish = "The prompt changes from `root@localhost:~#` back to Termux `~ $`.",
            impactLevel = ImpactLevel.READ_ONLY,
            verificationStatus = VerificationStatus.VERIFIED,
            isPinnedDaily = true
        )
    )

    val workflows: List<GuidedWorkflow> = listOf(
        GuidedWorkflow(
            id = "workflow-mobile-standalone-operator",
            title = "Mobile-Only Navigation: Operating Termux & Debian PRoot",
            summaryPlainEnglish = "5-step mobile checklist to explore phone storage, navigate folders, enter Debian Linux, work on projects, and return safely without a PC.",
            steps = listOf(
                GuidedWorkflowStep(
                    stepNumber = 1,
                    title = "Grant Phone Storage Access in Termux",
                    targetDeviceName = "Google Pixel 7 Pro",
                    environmentType = ExecutionEnvironmentType.TERMUX_HOST,
                    explanationPlainEnglish = "First grant Termux permission to see your phone's internal storage and Downloads folder.",
                    commandToRun = "termux-setup-storage",
                    expectedResult = "Android permission popup appears. Tap Allow.",
                    impactLevel = ImpactLevel.CONFIG_CHANGE,
                    ifItFailsHint = "If permission dialog does not appear, go to Android Settings -> Apps -> Termux -> Permissions and enable Photos and Videos / Files."
                ),
                GuidedWorkflowStep(
                    stepNumber = 2,
                    title = "Explore Phone Downloads Folder",
                    targetDeviceName = "Google Pixel 7 Pro",
                    environmentType = ExecutionEnvironmentType.TERMUX_HOST,
                    explanationPlainEnglish = "Navigate into your phone's Downloads directory and inspect downloaded files.",
                    commandToRun = "cd ~/storage/downloads && pwd && ls -lh",
                    expectedResult = "You see your downloaded files listed with file sizes.",
                    impactLevel = ImpactLevel.READ_ONLY,
                    ifItFailsHint = "If folder not found, verify step 1 was completed so `~/storage` symlink exists."
                ),
                GuidedWorkflowStep(
                    stepNumber = 3,
                    title = "Switch into Debian PRoot Linux",
                    targetDeviceName = "Google Pixel 7 Pro",
                    environmentType = ExecutionEnvironmentType.TERMUX_HOST,
                    explanationPlainEnglish = "Launch the Debian Linux container inside Termux.",
                    commandToRun = "proot-distro login debian",
                    expectedResult = "Terminal prompt changes from '~ \$' to 'root@localhost:~#'.",
                    impactLevel = ImpactLevel.READ_ONLY,
                    ifItFailsHint = "If proot-distro is missing, run `pkg install proot-distro -y` in Termux first."
                ),
                GuidedWorkflowStep(
                    stepNumber = 4,
                    title = "Navigate & Inspect Debian Linux Workspace",
                    targetDeviceName = "Google Pixel 7 Pro",
                    environmentType = ExecutionEnvironmentType.DEBIAN_PROOT,
                    explanationPlainEnglish = "Inside Debian, enter `/root` and check your project files and git status.",
                    commandToRun = "cd /root && pwd && ls -la",
                    expectedResult = "You see Debian root directory contents and projects.",
                    impactLevel = ImpactLevel.READ_ONLY,
                    ifItFailsHint = "Confirm your screen prompt ends with '#' indicating you are inside Debian."
                ),
                GuidedWorkflowStep(
                    stepNumber = 5,
                    title = "Exit Debian and Return to Termux Host",
                    targetDeviceName = "Google Pixel 7 Pro",
                    environmentType = ExecutionEnvironmentType.DEBIAN_PROOT,
                    explanationPlainEnglish = "When done working in Linux, exit back to the outer Termux shell.",
                    commandToRun = "exit",
                    expectedResult = "Prompt changes back from '#' to '~ \$'.",
                    impactLevel = ImpactLevel.READ_ONLY,
                    ifItFailsHint = "You can also press Ctrl+D in Termux to exit the current shell session."
                )
            )
        ),
        GuidedWorkflow(
            id = "workflow-ssh-full-session",
            title = "Start Phone SSH & Enter Debian from Windows 11",
            summaryPlainEnglish = "4-step checklist to wake up Termux on your Pixel 7 Pro, connect from your Windows 11 laptop, and switch safely into Debian.",
            steps = listOf(
                GuidedWorkflowStep(
                    stepNumber = 1,
                    title = "Wake Termux & Start SSH Server on Phone",
                    targetDeviceName = "Google Pixel 7 Pro",
                    environmentType = ExecutionEnvironmentType.TERMUX_HOST,
                    explanationPlainEnglish = "Open Termux on your phone and start the wake-lock and SSH server.",
                    commandToRun = "termux-wake-lock && sshd",
                    expectedResult = "Notification shows Termux wake-lock is active.",
                    impactLevel = ImpactLevel.CONFIG_CHANGE,
                    ifItFailsHint = "If `sshd` says command not found, install OpenSSH in Termux with `pkg install openssh`."
                ),
                GuidedWorkflowStep(
                    stepNumber = 2,
                    title = "Check Your Phone's Current Wi-Fi IP",
                    targetDeviceName = "Google Pixel 7 Pro",
                    environmentType = ExecutionEnvironmentType.TERMUX_HOST,
                    explanationPlainEnglish = "Still in Termux on your phone, check the IPv4 address assigned by your Wi-Fi router.",
                    commandToRun = "ip -4 addr show wlan0 | grep inet",
                    expectedResult = "You see `inet 192.168.x.x/24`.",
                    impactLevel = ImpactLevel.READ_ONLY,
                    ifItFailsHint = "If nothing appears, verify Wi-Fi is turned on and connected on your Pixel 7 Pro."
                ),
                GuidedWorkflowStep(
                    stepNumber = 3,
                    title = "Connect from Windows 11 PowerShell",
                    targetDeviceName = "Windows 11 Laptop",
                    environmentType = ExecutionEnvironmentType.WINDOWS_POWERSHELL,
                    explanationPlainEnglish = "On your Windows 11 laptop, connect to your phone's IP using port 8022.",
                    commandToRun = "ssh -p 8022 <PHONE_WIFI_IP>",
                    expectedResult = "You see the Termux welcome screen and `~ \$` prompt on your laptop.",
                    impactLevel = ImpactLevel.READ_ONLY,
                    ifItFailsHint = "Make sure both laptop and phone are on the same Wi-Fi network (not a guest Wi-Fi)."
                ),
                GuidedWorkflowStep(
                    stepNumber = 4,
                    title = "Switch from Termux Host into Debian proot",
                    targetDeviceName = "Google Pixel 7 Pro (via Windows SSH)",
                    environmentType = ExecutionEnvironmentType.TERMUX_HOST,
                    explanationPlainEnglish = "Now that you are logged into Termux, launch your Debian environment.",
                    commandToRun = "proot-distro login debian",
                    expectedResult = "Prompt changes to `root@localhost:~#`.",
                    impactLevel = ImpactLevel.READ_ONLY,
                    ifItFailsHint = "Run `proot-distro list` in Termux to check installed Linux distributions."
                )
            )
        )
    )

    val troubleshootingCases: List<TroubleshootingCase> = listOf(
        TroubleshootingCase(
            id = "case-ssh-connection-refused",
            symptomTitle = "Windows SSH Says 'Connection refused' on Port 8022",
            exactErrorSnippet = "ssh: connect to host 192.168.x.x port 8022: Connection refused",
            targetDeviceName = "Google Pixel 7 Pro",
            environmentType = ExecutionEnvironmentType.TERMUX_HOST,
            plainEnglishWhyItHappens = "Your Windows laptop reached your phone over Wi-Fi, but the Termux SSH server (`sshd`) is not running (often because the phone rebooted or Android put Termux to sleep).",
            safeReadOnlyCheckCommand = "pgrep -l sshd",
            verifiedFixCommand = "termux-wake-lock && sshd",
            verificationStatus = VerificationStatus.VERIFIED
        ),
        TroubleshootingCase(
            id = "case-apt-in-termux",
            symptomTitle = "Package Not Found or Permission Warning When Running `apt`",
            exactErrorSnippet = "E: Unable to locate package ... / Running apt in Termux",
            targetDeviceName = "Google Pixel 7 Pro",
            environmentType = ExecutionEnvironmentType.DEBIAN_PROOT,
            plainEnglishWhyItHappens = "You are likely still in the outer Termux Host shell (`~ \$`) instead of inside Debian (`root@localhost:~#`). Termux uses `pkg`, whereas standard Debian packages require entering Debian first.",
            safeReadOnlyCheckCommand = "cat /etc/os-release",
            verifiedFixCommand = "proot-distro login debian",
            verificationStatus = VerificationStatus.VERIFIED
        ),
        TroubleshootingCase(
            id = "case-proot-dns-resolution",
            symptomTitle = "Debian proot Cannot Reach Internet ('Temporary failure in name resolution')",
            exactErrorSnippet = "Could not resolve 'deb.debian.org' / Temporary failure in name resolution",
            targetDeviceName = "Google Pixel 7 Pro",
            environmentType = ExecutionEnvironmentType.DEBIAN_PROOT,
            plainEnglishWhyItHappens = "When switching Wi-Fi networks or reconnecting, Android's DNS resolver settings didn't copy into Debian proot's `/etc/resolv.conf` file.",
            safeReadOnlyCheckCommand = "cat /etc/resolv.conf",
            verifiedFixCommand = "echo \"nameserver 8.8.8.8\" > /etc/resolv.conf",
            verificationStatus = VerificationStatus.VERIFIED
        ),
        TroubleshootingCase(
            id = "case-battery-killer",
            symptomTitle = "SSH Session Freezes or Disconnects After Screen Turns Off",
            exactErrorSnippet = "packet_write_wait: Connection to 192.168.x.x port 8022: Broken pipe",
            targetDeviceName = "Google Pixel 7 Pro",
            environmentType = ExecutionEnvironmentType.TERMUX_HOST,
            plainEnglishWhyItHappens = "Android Doze or Battery Optimization put Termux into deep sleep because `termux-wake-lock` was not activated before locking the phone.",
            safeReadOnlyCheckCommand = "dumpsys battery",
            verifiedFixCommand = "termux-wake-lock && sshd",
            verificationStatus = VerificationStatus.VERIFIED
        ),
        TroubleshootingCase(
            id = "case-git-ssh-key",
            symptomTitle = "Git Push Fails With 'Permission denied (publickey)'",
            exactErrorSnippet = "git@github.com: Permission denied (publickey). fatal: Could not read from remote repository.",
            targetDeviceName = "Windows 11 Laptop",
            environmentType = ExecutionEnvironmentType.WINDOWS_POWERSHELL,
            plainEnglishWhyItHappens = "Your SSH client didn't load your SSH private key, or the matching public key is not registered under your GitHub account.",
            safeReadOnlyCheckCommand = "ssh -T git@github.com",
            verifiedFixCommand = "ssh-add \$HOME/.ssh/id_rsa && ssh -T git@github.com",
            verificationStatus = VerificationStatus.VERIFIED
        )
    )
}
