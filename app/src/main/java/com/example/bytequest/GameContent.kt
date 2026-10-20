package com.example.bytequest

/** Plain Kotlin learning content and puzzle rules, independent of Android Views. */
enum class PuzzleType { BINARY, GATE, CHOICE, ORDER, MATCH }

data class Puzzle(
    val topicId: String,
    val title: String,
    val instructions: String,
    val hint: String,
    val explanation: String,
    val type: PuzzleType,
    val target: Int = 0,
    val gate: String = "",
    val options: List<String> = emptyList(),
    val correctIndex: Int = -1,
    /** ORDER: display tiles in scrambled order, then rearrange to [correctOrder]. */
    val pieces: List<String> = emptyList(),
    val correctOrder: List<Int> = emptyList(),
    /** MATCH: pieces = components; descriptions = slots. Each component maps to slot index. */
    val descriptions: List<String> = emptyList(),
    val correctMatches: List<Int> = emptyList(),
    val subTargets: List<Int> = emptyList(),
    val subPrompts: List<String> = emptyList(),
    val subOptionsList: List<List<String>> = emptyList(),
    val subCorrectIndices: List<Int> = emptyList(),
    val subExplanations: List<String> = emptyList()
)

data class Lesson(
    val id: String, val name: String, val subtitle: String, val summary: String,
    val keyIdeas: List<String>, val example: String, val studyTip: String
)

data class Challenge(val topicId: String, val count: Int) {
    /** Keep legacy BQ4 prefix for compatibility with already shared classroom codes. */
    val code: String get() = "BQ4-$topicId-$count"
}

object GameContent {
    val lessons = listOf(
        Lesson("NUM", "Number Systems", "Binary and decimal",
            "Computers store and process information as bits. Binary uses only 0 and 1; each bit position has a value that doubles as you move left.",
            listOf(
                "Four bit places from left to right: 8, 4, 2, 1.",
                "A bit contributes its place value only when it is 1.",
                "Add active place values to convert binary to decimal.",
                "Binary uses the digits 0 and 1 only (base 2).",
                "For n binary bits, there are 2^n different patterns. From the right, place values are 1, 2, 4, 8, 16, 32, and so on.",
                "One byte contains eight bits.",
                "Decimal is base 10 and uses digits 0 to 9.",
                "Hexadecimal is base 16, uses 0–9 and A–F, and one hex digit represents four bits.",
                "Leading zero bits do not change the value of a nonnegative binary number.",
                "Binary addition and subtraction follow ordinary arithmetic; convert to decimal or add using carrying.",
                "For unsigned values without overflow, shifting left one bit doubles and shifting right one bit halves with any remainder discarded."
            ),
            "0101₂ = 0×8 + 1×4 + 0×2 + 1×1 = 5₁₀.",
            "Convert 9, 10 and 13 using four bit switches before trying a level."),
        Lesson("LOG", "Digital Logic", "AND, OR, XOR gates",
            "Logic gates process binary inputs. Their outputs follow precise rules that are used to build digital circuits.",
            listOf(
                "AND returns 1 only if both inputs are 1.",
                "OR returns 1 if at least one input is 1.",
                "XOR returns 1 if the inputs are different.",
                "NOT is an inverter: NOT 0 = 1 and NOT 1 = 0. An output bubble indicates inversion.",
                "NAND means NOT AND: it is 0 only when all inputs are 1.",
                "NOR means NOT OR: it is 1 only when all inputs are 0.",
                "XNOR means NOT XOR: it is 1 when its two inputs are equal.",
                "Truth tables list every input combination: two inputs have 4 rows and three have 8. Logic 1 means true/high.",
                "For combined expressions, evaluate the inner gates first, then apply NOT, AND, or OR to the intermediate results."
            ),
            "For A=1, B=0: AND=0, OR=1, XOR=1.",
            "Draw the four input pairs (00, 01, 10, 11) and write each gate's output."),
        Lesson("PRG", "Programming Basics", "Operators and code order",
            "Programs execute instructions in an order. Variables store values, and expressions follow precedence rules.",
            listOf(
                "Kotlin's val declares a read-only reference.",
                "Multiplication and integer division/remainder happen before addition unless parentheses change the order.",
                "A variable must be declared before it is used in the same scope.",
                "println displays text or calculated values in the console.",
                "Kotlin var declares a mutable variable; assignment and compound updates such as += change its value.",
                "Kotlin types include Int (whole numbers), Double (decimal values), Boolean (true/false), and String (text). Strings use double quotes and + joins strings.",
                "Declare functions with fun and call them using their name followed by parentheses.",
                "A Kotlin single-line comment starts with //.",
                "if / else chooses branches using Boolean expressions, including == for equality and >= for inclusive comparison.",
                "for loops, repeat(n), and while loops repeat work; 1..3 includes 3 while 0 until 3 excludes 3. break leaves a loop and continue skips an iteration.",
                "listOf(...) creates a list, and Kotlin list indices start at 0."
            ),
            "val a = 3; val b = 4; val result = a + b; println(result) prints 7.",
            "Trace an expression on paper, then arrange declarations before their uses."),
        Lesson("DBG", "Debugging", "Conditions and comparison",
            "Debugging means finding and fixing problems in code. Compare expected behavior with actual results.",
            listOf(
                "= assigns a value; == checks equality in Kotlin. The != operator tests inequality.",
                ">= means greater than or equal to.",
                "Test conditions just below, at, and above the boundary.",
                "Strict comparisons < and > exclude equality; <= and >= include equality.",
                "Syntax and type errors, such as missing delimiters or undeclared identifiers, are generally reported at compile time.",
                "Runtime errors occur during execution, such as dividing an integer by zero or accessing an invalid list index.",
                "Logic bugs produce wrong behavior while code runs; checking calculations and save/read logic helps isolate them.",
                "Unit tests target small functions; regression tests confirm that previously working behavior still works after a change.",
                "Nullable values must be checked or accessed safely; Kotlin ?. is a safe call while !! can throw for null.",
                "Use logs, stack traces, and breakpoints to inspect execution and diagnose Android application failures.",
                "Reproduce bugs with consistent inputs, record expected versus actual results, and change one suspected cause at a time."
            ),
            "score >= 10 is true for 10 or 12, but false for 9.",
            "Try a boundary test whenever a condition uses <, >, <= or >=."),
        Lesson("NET", "Networking", "Addresses, DNS and requests",
            "Computer networks connect devices and services. DNS helps find IP addresses for names, and IP routing moves traffic between networks.",
            listOf(
                "A device needs connectivity and typically an IP configuration.",
                "DNS resolves host names into IP addresses; DNS A records hold IPv4 and AAAA records hold IPv6 addresses.",
                "Web requests can follow once the destination address is known.",
                "IP addresses identify network endpoints; IPv4 uses 32 bits, IPv6 uses 128, and subnets group network addresses.",
                "DHCP automatically leases IP addressing and related network configuration to clients.",
                "Routers move packets between networks, switches connect local Ethernet devices, and a default gateway reaches remote networks.",
                "LAN means local-area network; WAN refers to a network spanning a wider region.",
                "A browser exchanges HTTP requests and responses; HTTPS protects traffic with TLS, and a URL identifies a resource.",
                "Wi-Fi connects wirelessly and Ethernet connects by cable; access points join wireless clients to local networks.",
                "Bandwidth is data capacity per second, latency is delay, and ping can test reachability when ICMP responses are allowed.",
                "Networks transport packets; packet loss is missing delivered packets, and uploading sends a local file to a remote system."
            ),
            "Connect → get IP configuration → resolve example.com → send HTTPS request.",
            "Check connectivity and name resolution separately when troubleshooting."),
        Lesson("ALG", "Algorithms & Flowcharts", "Ordered steps and processes",
            "An algorithm describes steps that solve a problem. Flowcharts show these steps visually, with a clear start and end.",
            listOf(
                "Start and End are terminators.",
                "Input comes before processing when the process needs that input.",
                "The display/output step must use a calculated value after the calculation.",
                "An algorithm is an ordered process for solving a task; a flowchart shows the steps visually.",
                "A diamond represents decision or selection, often with Yes/No branches.",
                "A rectangle represents processing, such as calculating total = a + b.",
                "A parallelogram commonly represents input or output.",
                "Flowchart arrows show the direction and sequence of control flow.",
                "Iteration repeats steps using loops, counters, and conditions; a terminating loop eventually reaches an end.",
                "Pseudocode describes steps in readable language without requiring exact Kotlin syntax.",
                "Algorithms define inputs, processing, outputs, and working variables to hold intermediate values.",
                "A dry run traces an algorithm manually with sample values to check each step."
            ),
            "Start → Read number → Square = number × number → Display square → End.",
            "Check that every value is created before any step uses it."),
        Lesson("HWR", "Computer Hardware", "Components and their functions",
            "Computer components play different roles. Matching each physical part to its role makes troubleshooting and assembly easier.",
            listOf(
                "CPU executes instructions and carries out arithmetic and control operations.",
                "RAM holds temporary working data, while an SSD keeps files without power.",
                "A PSU provides electrical power to components.",
                "A motherboard connects the CPU, RAM and expansion parts; physical slots and memory specifications must be compatible.",
                "A GPU specializes in graphics rendering and acceleration.",
                "Fans, heatsinks and thermal interface materials help move heat away from components.",
                "Keyboards, microphones and scanners are input devices; monitors, speakers and printers are output devices.",
                "SSDs and HDDs store data without power. HDDs use spinning disks; SSDs use flash memory and may connect through SATA or NVMe/PCIe.",
                "USB connects many peripherals, HDMI carries digital video/audio, and PCIe connects compatible expansion cards.",
                "BIOS/UEFI firmware initializes hardware and starts the boot process before an operating system loads from storage."
            ),
            "CPU → processing; RAM → temporary memory; SSD → storage; PSU → power.",
            "Identify the component and explain its role before looking at a PC diagram.")
    )

    /** Original eight levels stay in the same order for save-data compatibility. */
    val puzzles = listOf(
        Puzzle("NUM", "Binary Builder", "Make decimal 5 by turning the four bits on or off.",
            "Each bit is worth 8, 4, 2, or 1. Choose values that add up to 5.",
            "0101 in binary is 4 + 1 = 5.", PuzzleType.BINARY, target = 5,
            subTargets = listOf(5, 10, 3, 6, 7, 9, 11, 12, 13, 15)),
        Puzzle("NUM", "Binary Builder II", "Make decimal 10 using the four bits.",
            "Find two bit values that add up to 10.", "1010 in binary is 8 + 2 = 10.", PuzzleType.BINARY, target = 10,
            subTargets = listOf(10, 5, 3, 6, 7, 9, 11, 12, 13, 15)),
        Puzzle("LOG", "AND Gate", "Change A and B to make the AND output equal to 1.",
            "An AND gate outputs 1 only when BOTH inputs are 1.",
            "1 AND 1 = 1. Any other AND combination gives 0.", PuzzleType.GATE, target = 1, gate = "AND"),
        Puzzle("LOG", "OR Gate", "Change A and B to make the OR output equal to 1.",
            "An OR gate outputs 1 if at least one input is 1.",
            "0 OR 1 = 1. One or both inputs must be on.", PuzzleType.GATE, target = 1, gate = "OR"),
        Puzzle("LOG", "XOR Gate", "Change A and B to make the XOR output equal to 1.",
            "XOR outputs 1 when its two inputs are different.",
            "0 XOR 1 = 1 and 1 XOR 0 = 1.", PuzzleType.GATE, target = 1, gate = "XOR"),
        Puzzle("PRG", "Code Detective", "What does this Kotlin code print?\n\nval x = 3\nval y = 2\nprintln(x + y * 2)",
            "Multiplication happens before addition.", "2 × 2 = 4, then 3 + 4 = 7.",
            PuzzleType.CHOICE, options = listOf("10", "7", "12", "5"), correctIndex = 1,
            subPrompts = listOf(
                "What does this Kotlin code print?\n\nval x = 3\nval y = 2\nprintln(x + y * 2)",
                "What does this Kotlin code print?\n\nval a = 10\nval b = 3\nprintln(a % b)",
                "What is the output?\n\nval s = \"Byte\" + \"Quest\"\nprintln(s.length)",
                "What is printed?\n\nval arr = arrayOf(10, 20, 30)\nprintln(arr[1])",
                "What is returned?\n\nfun double(n: Int) = n * 2\nprintln(double(4))",
                "What is printed?\n\nval x = 5\nif (x > 3) println(\"A\") else println(\"B\")",
                "What is printed?\n\nvar count = 0\nfor(i in 1..3) count++\nprintln(count)",
                "What is the list size?\n\nval list = listOf(\"a\", \"b\")\nprintln(list.size)",
                "What is the output?\n\nval p = 15\nval q = 4\nprintln(p / q)",
                "What is printed?\n\nval name = \"Kotlin\"\nprintln(name.first())"
            ),
            subOptionsList = listOf(
                listOf("10", "7", "12", "5"),
                listOf("1", "3", "0", "3.33"),
                listOf("9", "8", "10", "4"),
                listOf("10", "20", "30", "0"),
                listOf("4", "6", "8", "16"),
                listOf("A", "B", "AB", "Nothing"),
                listOf("2", "3", "4", "0"),
                listOf("1", "2", "3", "0"),
                listOf("3.75", "3", "4", "3.0"),
                listOf("K", "n", "Kotlin", "0")
            ),
            subCorrectIndices = listOf(1, 0, 0, 1, 2, 0, 1, 1, 1, 0),
            subExplanations = listOf(
                "2 × 2 = 4, then 3 + 4 = 7.",
                "10 divided by 3 gives remainder 1.",
                "\"ByteQuest\" contains 9 characters.",
                "Index 1 corresponds to the second element, 20.",
                "double(4) computes 4 × 2 = 8.",
                "Since 5 > 3 is true, 'A' is printed.",
                "The loop increments count 3 times.",
                "The list has 2 items.",
                "Integer division 15 / 4 truncates to 3.",
                "first() returns 'K'."
            )),
        Puzzle("DBG", "Fix the Condition", "Which expression correctly checks whether score is at least 10?",
            "Use the greater-than-or-equal comparison operator.",
            ">= compares whether the left value is greater than or equal to the right value.",
            PuzzleType.CHOICE, options = listOf("score = 10", "score >== 10", "score >= 10", "score => 10"), correctIndex = 2,
            subPrompts = listOf(
                "Which expression correctly checks whether score is at least 10?",
                "Which operator checks value equality in Kotlin?",
                "Which operator checks logical NOT in Kotlin?",
                "How do you write 'A AND B' in Kotlin?",
                "How do you write 'A OR B' in Kotlin?",
                "Which condition checks if x is between 1 and 10 inclusive?",
                "Which operator checks inequality (not equal)?",
                "What does 'score++' do?",
                "Which condition is true when x is negative?",
                "What evaluates to true when 'active' is false?"
            ),
            subOptionsList = listOf(
                listOf("score = 10", "score >== 10", "score >= 10", "score => 10"),
                listOf("=", "==", "===", ":="),
                listOf("NOT", "!", "~", "no"),
                listOf("A & B", "A AND B", "A && B", "A and B"),
                listOf("A || B", "A OR B", "A | B", "A or B"),
                listOf("1 <= x <= 10", "x >= 1 && x <= 10", "x in 1..10", "x >= 1 || x <= 10"),
                listOf("!=", "<>", "=/=", "not="),
                listOf("Decrements score by 1", "Increments score by 1", "Doubles score", "Resets score"),
                listOf("x < 0", "x > 0", "x == 0", "x != 0"),
                listOf("active", "!active", "active == true", "active != false")
            ),
            subCorrectIndices = listOf(2, 1, 1, 2, 0, 1, 0, 1, 0, 1),
            subExplanations = listOf(
                ">= compares whether score is greater than or equal to 10.",
                "== checks structural equality.",
                "! is the logical NOT operator.",
                "&& is the logical AND operator.",
                "|| is the logical OR operator.",
                "x >= 1 && x <= 10 checks both boundaries.",
                "!= checks inequality.",
                "score++ increments score by 1.",
                "x < 0 is true for negative numbers.",
                "!active converts false to true."
            )),
        Puzzle("NET", "Network Rescue", "A website works using its IP address, but not its domain name. Which service might be failing?",
            "Think about which service translates names like example.com into IP addresses.",
            "DNS translates domain names into IP addresses.",
            PuzzleType.CHOICE, options = listOf("DNS", "Bluetooth", "HDMI", "USB"), correctIndex = 0,
            subPrompts = listOf(
                "A website works using its IP address, but not its domain name. Which service failed?",
                "Which protocol secures HTTP web traffic with encryption?",
                "Which device connects multiple networks together and routes traffic?",
                "What type of address identifies a physical network card globally?",
                "Which protocol automatically assigns IP addresses to devices on a network?",
                "What is the standard IPv4 loopback address for localhost?",
                "Which transport protocol guarantees ordered, error-checked packet delivery?",
                "Which transport protocol is lightweight and used for real-time video streaming?",
                "What tool tests network connectivity to a remote host?",
                "Which network topology connects all devices to a central hub or switch?"
            ),
            subOptionsList = listOf(
                listOf("DNS", "Bluetooth", "HDMI", "USB"),
                listOf("HTTPS", "FTP", "UDP", "SMTP"),
                listOf("Router", "Monitor", "Printer", "Keyboard"),
                listOf("MAC address", "IP address", "URL", "Port number"),
                listOf("DHCP", "DNS", "HTTP", "SSH"),
                listOf("127.0.0.1", "192.168.1.1", "255.255.255.0", "10.0.0.1"),
                listOf("TCP", "UDP", "IP", "ICMP"),
                listOf("UDP", "TCP", "HTTP", "FTP"),
                listOf("ping", "grep", "chmod", "mkdir"),
                listOf("Star", "Bus", "Ring", "Mesh")
            ),
            subCorrectIndices = listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
            subExplanations = listOf(
                "DNS translates domain names into IP addresses.",
                "HTTPS uses TLS/SSL encryption.",
                "Routers connect networks and route packets.",
                "MAC addresses identify network hardware.",
                "DHCP dynamically assigns IP addresses.",
                "127.0.0.1 is the IPv4 loopback address.",
                "TCP provides reliable, ordered stream delivery.",
                "UDP has lower latency for video/gaming.",
                "ping sends ICMP Echo requests to test connectivity.",
                "Star topology uses a central switch or hub."
            )),
        Puzzle("ALG", "Flowchart Builder", "Rearrange the flowchart tiles to compute and show a number's square.",
            "Begin with Start. Read the input before you calculate it, display the result, then End.",
            "Start → Read number → Square = number × number → Display square → End.", PuzzleType.ORDER,
            pieces = listOf("Display square", "Start", "Read number", "End", "Square = number × number"),
            correctOrder = listOf(1, 2, 4, 0, 3)),
        Puzzle("PRG", "Code Block Builder", "Place the Kotlin lines in a valid sequence to print 7.",
            "Declare a and b first, calculate the result next, and print last.",
            "val a = 3; val b = 4; val result = a + b; println(result) prints 7.", PuzzleType.ORDER,
            pieces = listOf("println(result)", "val a = 3", "val result = a + b", "val b = 4"),
            correctOrder = listOf(1, 3, 2, 0)),
        Puzzle("HWR", "Hardware Match Lab", "Match each component to its correct function. Tap a component and then a function, or drag it onto the function.",
            "CPU processes, RAM works temporarily, SSD stores long-term files, and PSU supplies power.",
            "CPU: processing; RAM: temporary memory; SSD: persistent storage; PSU: electrical power.", PuzzleType.MATCH,
            pieces = listOf("CPU", "RAM", "SSD", "PSU"),
            descriptions = listOf("Stores files without power", "Supplies power to components", "Executes program instructions", "Holds temporary working data"),
            correctMatches = listOf(2, 3, 0, 1)),
        Puzzle("NET", "Packet Journey", "Rearrange these networking steps into a sensible web-browsing sequence.",
            "Connect first, receive an IP configuration, look up the hostname, then send the request.",
            "Connect → obtain IP configuration → resolve DNS → send HTTPS request.", PuzzleType.ORDER,
            pieces = listOf("Send HTTPS request", "Obtain IP configuration", "Connect to network", "Resolve hostname using DNS"),
            correctOrder = listOf(2, 1, 3, 0)),
        Puzzle("NUM", "Binary Builder III", "Make decimal 12 using the four bits.",
            "Find two bit values that add up to 12 (8 + 4).",
            "1100 in binary is 8 + 4 = 12.", PuzzleType.BINARY, target = 12),
        Puzzle("LOG", "NAND Gate", "Change A and B to make the NAND output equal to 0.",
            "NAND outputs 0 only when BOTH inputs are 1.",
            "NAND is the opposite of AND: 1 NAND 1 = 0.", PuzzleType.GATE, target = 0, gate = "NAND"),
        Puzzle("LOG", "NOR Gate", "Change A and B to make the NOR output equal to 1.",
            "NOR outputs 1 only when BOTH inputs are 0.",
            "NOR is the opposite of OR: 0 NOR 0 = 1.", PuzzleType.GATE, target = 1, gate = "NOR"),
        Puzzle("PRG", "Loop Sum", "What does this Kotlin snippet print?\n\nvar total = 0\nfor (i in 1..4) total += i\nprintln(total)",
            "Add numbers from 1 to 4 sequentially.", "1 + 2 + 3 + 4 = 10.",
            PuzzleType.CHOICE, options = listOf("6", "10", "15", "8"), correctIndex = 1),
        Puzzle("DBG", "Type Mismatch", "Which Kotlin assignment will cause a compile-time type mismatch error?",
            "Variables declared with a specific type cannot be assigned an incompatible type literal.",
            "var score: Int = \"10\" assigns a String to an Int variable.",
            PuzzleType.CHOICE, options = listOf("val x = 10", "var score: Int = \"10\"", "val name = \"Byte\"", "var active = true"), correctIndex = 1),
        Puzzle("NET", "Secure Web Port", "Which standard network port is used for secure HTTPS web traffic?",
            "HTTPS encrypts web traffic over port 443 (HTTP uses 80).",
            "Port 443 is standard for HTTPS.",
            PuzzleType.CHOICE, options = listOf("Port 80", "Port 22", "Port 443", "Port 21"), correctIndex = 2),
        Puzzle("ALG", "Bubble Sort", "Arrange the Bubble Sort steps into the correct sequence.",
            "Start by comparing adjacent items, swapping if necessary, and repeat until no swaps are needed.",
            "Start → Compare adjacent items → Swap if in wrong order → Repeat until no swaps needed → Sorted list.", PuzzleType.ORDER,
            pieces = listOf("Compare adjacent items", "Start", "Repeat until no swaps needed", "Swap if in wrong order", "Sorted list"),
            correctOrder = listOf(1, 0, 3, 2, 4)),
        Puzzle("HWR", "Component Match II", "Match each hardware component to its correct description.",
            "Motherboard connects parts, GPU renders graphics, HDD stores data magnetically, Cooling Fan dissipates heat.",
            "Motherboard: circuits; GPU: graphics; HDD: magnetic storage; Cooling Fan: heat dissipation.", PuzzleType.MATCH,
            pieces = listOf("Motherboard", "GPU", "HDD", "Cooling Fan"),
            descriptions = listOf("Renders graphics and video", "Main circuit board connecting parts", "Magnetic spinning disk storage", "Dissipates CPU heat"),
            correctMatches = listOf(1, 0, 2, 3)),
        Puzzle("PRG", "Function Return", "What is the return value of compute(5) given:\n\nfun compute(n: Int): Int {\n    return n * 3\n}",
            "Multiply 5 by 3.", "5 * 3 = 15.",
            PuzzleType.CHOICE, options = listOf("15", "12", "8", "18"), correctIndex = 0),
        Puzzle("NET", "IPv4 Address", "How many bits are used in a standard IPv4 address?",
            "IPv4 addresses use 32 bits, expressed as four octets.",
            "IPv4 addresses consist of 32 bits (4 bytes).",
            PuzzleType.CHOICE, options = listOf("16 bits", "32 bits", "64 bits", "128 bits"), correctIndex = 1),
        Puzzle("NUM", "Binary Builder IV", "Make decimal 3 using the four bits.", "Find bit values that add up to 3 (2 + 1).", "0011 in binary is 2 + 1 = 3.", PuzzleType.BINARY, target = 3),
        Puzzle("NUM", "Binary Builder V", "Make decimal 6 using the four bits.", "Find bit values that add up to 6 (4 + 2).", "0110 in binary is 4 + 2 = 6.", PuzzleType.BINARY, target = 6),
        Puzzle("NUM", "Binary Builder VI", "Make decimal 7 using the four bits.", "Find bit values that add up to 7 (4 + 2 + 1).", "0111 in binary is 4 + 2 + 1 = 7.", PuzzleType.BINARY, target = 7),
        Puzzle("NUM", "Binary Builder VII", "Make decimal 9 using the four bits.", "Find bit values that add up to 9 (8 + 1).", "1001 in binary is 8 + 1 = 9.", PuzzleType.BINARY, target = 9),
        Puzzle("NUM", "Binary Builder VIII", "Make decimal 11 using the four bits.", "Find bit values that add up to 11 (8 + 2 + 1).", "1011 in binary is 8 + 2 + 1 = 11.", PuzzleType.BINARY, target = 11),
        Puzzle("NUM", "Binary Builder IX", "Make decimal 13 using the four bits.", "Find bit values that add up to 13 (8 + 4 + 1).", "1101 in binary is 8 + 4 + 1 = 13.", PuzzleType.BINARY, target = 13),
        Puzzle("NUM", "Binary Builder X", "Make decimal 14 using the four bits.", "Find bit values that add up to 14 (8 + 4 + 2).", "1110 in binary is 8 + 4 + 2 = 14.", PuzzleType.BINARY, target = 14),
        Puzzle("NUM", "Binary Builder XI", "Make decimal 15 using the four bits.", "All four bits should be on.", "1111 in binary is 8 + 4 + 2 + 1 = 15.", PuzzleType.BINARY, target = 15),
        Puzzle("NUM", "Binary Builder XII", "Make decimal 1 using the four bits.", "Turn on the rightmost bit.", "0001 in binary equals 1.", PuzzleType.BINARY, target = 1),
        Puzzle("NUM", "Binary Builder XIII", "Make decimal 2 using the four bits.", "Turn on the second bit from right.", "0010 in binary equals 2.", PuzzleType.BINARY, target = 2),
        Puzzle("LOG", "XNOR Gate", "Change A and B to make the XNOR output equal to 1.", "XNOR outputs 1 when inputs are equal.", "0 XNOR 0 = 1 and 1 XNOR 1 = 1.", PuzzleType.GATE, target = 1, gate = "XNOR"),
        Puzzle("LOG", "Buffer Gate", "Change input A to make the Buffer output equal to 1.", "Buffer outputs the same value as its input.", "Buffer 1 = 1.", PuzzleType.GATE, target = 1, gate = "BUFFER"),
        Puzzle("PRG", "Array Indexing", "In Kotlin, what is the index of the first element in a list?", "Lists in Kotlin are 0-indexed.", "The first element is at index 0.", PuzzleType.CHOICE, options = listOf("0", "1", "3", "4"), correctIndex = 0),
        Puzzle("PRG", "String Concatenation", "What does \"Byte\" + \"Quest\" evaluate to in Kotlin?", "Plus operator joins strings together.", "\"Byte\" + \"Quest\" results in \"ByteQuest\".", PuzzleType.CHOICE, options = listOf("ByteQuest", "Byte Quest", "Byte+Quest", "Error"), correctIndex = 0),
        Puzzle("DBG", "Infinite Loop", "Which construct creates a loop that runs continuously unless explicitly broken?", "A loop with a constant true condition runs indefinitely.", "while(true) is an infinite loop.", PuzzleType.CHOICE, options = listOf("while(true)", "if(x > 0)", "val x = 1", "return x"), correctIndex = 0),
        Puzzle("NET", "MAC Address", "What is a MAC address?", "MAC stands for Media Access Control, identifying network hardware.", "A MAC address is a physical hardware identifier for network interfaces.", PuzzleType.CHOICE, options = listOf("Physical hardware address", "IP address", "Domain name", "Port number"), correctIndex = 0),
        Puzzle("ALG", "Binary Search", "Arrange the Binary Search steps in correct order.", "Check middle element, compare with target, halve search space until found.", "Sorted array → Check middle element → Halve search space → Target found.", PuzzleType.ORDER, pieces = listOf("Check middle element", "Target found", "Halve search space", "Sorted array"), correctOrder = listOf(3, 0, 2, 1)),
        Puzzle("HWR", "RAM vs ROM", "Match each memory component to its correct description.", "RAM is volatile working memory; ROM stores startup BIOS.", "RAM: volatile working memory; ROM: non-volatile BIOS storage; CPU: calculations; SSD: persistent storage.", PuzzleType.MATCH, pieces = listOf("RAM", "ROM", "CPU", "SSD"), descriptions = listOf("Non-volatile startup BIOS storage", "Volatile working memory", "Executes calculations", "Persistent storage"), correctMatches = listOf(1, 0, 2, 3))
    )

    val allTopics: List<String> = listOf("ALL") + lessons.map { it.id }

    fun topicName(topicId: String): String = if (topicId == "ALL") "All topics"
        else lessons.firstOrNull { it.id == topicId }?.name ?: "Unknown topic"

    fun puzzleIndices(topicId: String): List<Int> = puzzles.indices.filter {
        topicId == "ALL" || puzzles[it].topicId == topicId
    }

    fun makeChallenge(topicId: String, count: Int): Challenge? {
        val total = puzzleIndices(topicId).size
        return if (topicId in allTopics && count in 1..total) Challenge(topicId, count) else null
    }

    fun parseChallenge(raw: String): Challenge? {
        val parts = raw.trim().uppercase().split('-')
        if (parts.size != 3 || parts[0] != "BQ4") return null
        val count = parts[2].toIntOrNull() ?: return null
        return makeChallenge(parts[1], count)
    }

    fun challengePuzzleIndices(challenge: Challenge): List<Int> =
        puzzleIndices(challenge.topicId).take(challenge.count)

    fun binaryValue(bits: IntArray): Int {
        if (bits.size != 4 || bits.any { it != 0 && it != 1 }) return -1
        val weights = intArrayOf(8, 4, 2, 1)
        return bits.indices.sumOf { bits[it] * weights[it] }
    }

    fun gateValue(gate: String, a: Int, b: Int): Int = when (gate) {
        "AND" -> a and b
        "OR" -> a or b
        "XOR" -> a xor b
        "NAND" -> if ((a and b) == 1) 0 else 1
        "NOR" -> if ((a or b) == 1) 0 else 1
        "XNOR" -> if ((a xor b) == 1) 0 else 1
        "BUFFER" -> a
        else -> -1
    }

    fun validPermutation(items: List<Int>, size: Int): Boolean =
        items.size == size && items.toSet() == (0 until size).toSet()

    fun check(puzzle: Puzzle, bits: IntArray, a: Int, b: Int, answer: Int,
              order: List<Int> = emptyList(), matches: List<Int> = emptyList(), subRound: Int = 0): Boolean = when (puzzle.type) {
        PuzzleType.BINARY -> {
            val t = if (puzzle.subTargets.isNotEmpty()) puzzle.subTargets.getOrElse(subRound) { puzzle.target } else puzzle.target
            binaryValue(bits) == t
        }
        PuzzleType.GATE -> {
            val t = if (puzzle.subTargets.isNotEmpty()) puzzle.subTargets.getOrElse(subRound) { puzzle.target } else puzzle.target
            a in 0..1 && b in 0..1 && gateValue(puzzle.gate, a, b) == t
        }
        PuzzleType.CHOICE -> {
            val correctIdx = if (puzzle.subCorrectIndices.isNotEmpty()) puzzle.subCorrectIndices.getOrElse(subRound) { puzzle.correctIndex } else puzzle.correctIndex
            val optionsSize = if (puzzle.subOptionsList.isNotEmpty()) puzzle.subOptionsList.getOrElse(subRound) { puzzle.options }.size else puzzle.options.size
            answer == correctIdx && correctIdx in 0 until optionsSize
        }
        PuzzleType.ORDER -> validPermutation(order, puzzle.pieces.size) && (
            order == puzzle.correctOrder ||
            (puzzle.title == "Code Block Builder" &&
                order.indexOf(1) < order.indexOf(2) &&
                order.indexOf(3) < order.indexOf(2) &&
                order.indexOf(2) < order.indexOf(0))
        )
        PuzzleType.MATCH -> matches.size == puzzle.pieces.size &&
            validPermutation(matches, puzzle.descriptions.size) && matches == puzzle.correctMatches
    }
}
