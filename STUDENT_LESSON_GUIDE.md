# ByteQuest — Seven Student Lessons & 350-Question Study Guide

Read a lesson before its 50-question quiz. Each question links back to one **numbered key idea** below. The game provides the explanation after every answer.

## Number Systems

1. Four bit places from left to right: 8, 4, 2, 1.
2. A bit contributes its place value only when it is 1.
3. Add active place values to convert binary to decimal.
4. Binary uses the digits 0 and 1 only (base 2).
5. For n binary bits, there are 2^n different patterns. From the right, place values are 1, 2, 4, 8, 16, 32, and so on.
6. One byte contains eight bits.
7. Decimal is base 10 and uses digits 0 to 9.
8. Hexadecimal is base 16, uses 0–9 and A–F, and one hex digit represents four bits.
9. Leading zero bits do not change the value of a nonnegative binary number.
10. Binary addition and subtraction follow ordinary arithmetic; convert to decimal or add using carrying.
11. For unsigned values without overflow, shifting left one bit doubles and shifting right one bit halves with any remainder discarded.

**Practice:** 5 sets × 10 questions = 50 questions. Question IDs NUM01–NUM50.

## Digital Logic

1. AND returns 1 only if both inputs are 1.
2. OR returns 1 if at least one input is 1.
3. XOR returns 1 if the inputs are different.
4. NOT is an inverter: NOT 0 = 1 and NOT 1 = 0. An output bubble indicates inversion.
5. NAND means NOT AND: it is 0 only when all inputs are 1.
6. NOR means NOT OR: it is 1 only when all inputs are 0.
7. XNOR means NOT XOR: it is 1 when its two inputs are equal.
8. Truth tables list every input combination: two inputs have 4 rows and three have 8. Logic 1 means true/high.
9. For combined expressions, evaluate the inner gates first, then apply NOT, AND, or OR to the intermediate results.

**Practice:** 5 sets × 10 questions = 50 questions. Question IDs LOG01–LOG50.

## Programming Basics

1. Kotlin's val declares a read-only reference.
2. Multiplication and integer division/remainder happen before addition unless parentheses change the order.
3. A variable must be declared before it is used in the same scope.
4. println displays text or calculated values in the console.
5. Kotlin var declares a mutable variable; assignment and compound updates such as += change its value.
6. Kotlin types include Int (whole numbers), Double (decimal values), Boolean (true/false), and String (text). Strings use double quotes and + joins strings.
7. Declare functions with fun and call them using their name followed by parentheses.
8. A Kotlin single-line comment starts with //.
9. if / else chooses branches using Boolean expressions, including == for equality and >= for inclusive comparison.
10. for loops, repeat(n), and while loops repeat work; 1..3 includes 3 while 0 until 3 excludes 3. break leaves a loop and continue skips an iteration.
11. listOf(...) creates a list, and Kotlin list indices start at 0.

**Practice:** 5 sets × 10 questions = 50 questions. Question IDs PRG01–PRG50.

## Debugging

1. = assigns a value; == checks equality in Kotlin. The != operator tests inequality.
2. >= means greater than or equal to.
3. Test conditions just below, at, and above the boundary.
4. Strict comparisons < and > exclude equality; <= and >= include equality.
5. Syntax and type errors, such as missing delimiters or undeclared identifiers, are generally reported at compile time.
6. Runtime errors occur during execution, such as dividing an integer by zero or accessing an invalid list index.
7. Logic bugs produce wrong behavior while code runs; checking calculations and save/read logic helps isolate them.
8. Unit tests target small functions; regression tests confirm that previously working behavior still works after a change.
9. Nullable values must be checked or accessed safely; Kotlin ?. is a safe call while !! can throw for null.
10. Use logs, stack traces, and breakpoints to inspect execution and diagnose Android application failures.
11. Reproduce bugs with consistent inputs, record expected versus actual results, and change one suspected cause at a time.

**Practice:** 5 sets × 10 questions = 50 questions. Question IDs DBG01–DBG50.

## Networking

1. A device needs connectivity and typically an IP configuration.
2. DNS resolves host names into IP addresses; DNS A records hold IPv4 and AAAA records hold IPv6 addresses.
3. Web requests can follow once the destination address is known.
4. IP addresses identify network endpoints; IPv4 uses 32 bits, IPv6 uses 128, and subnets group network addresses.
5. DHCP automatically leases IP addressing and related network configuration to clients.
6. Routers move packets between networks, switches connect local Ethernet devices, and a default gateway reaches remote networks.
7. LAN means local-area network; WAN refers to a network spanning a wider region.
8. A browser exchanges HTTP requests and responses; HTTPS protects traffic with TLS, and a URL identifies a resource.
9. Wi-Fi connects wirelessly and Ethernet connects by cable; access points join wireless clients to local networks.
10. Bandwidth is data capacity per second, latency is delay, and ping can test reachability when ICMP responses are allowed.
11. Networks transport packets; packet loss is missing delivered packets, and uploading sends a local file to a remote system.

**Practice:** 5 sets × 10 questions = 50 questions. Question IDs NET01–NET50.

## Algorithms & Flowcharts

1. Start and End are terminators.
2. Input comes before processing when the process needs that input.
3. The display/output step must use a calculated value after the calculation.
4. An algorithm is an ordered process for solving a task; a flowchart shows the steps visually.
5. A diamond represents decision or selection, often with Yes/No branches.
6. A rectangle represents processing, such as calculating total = a + b.
7. A parallelogram commonly represents input or output.
8. Flowchart arrows show the direction and sequence of control flow.
9. Iteration repeats steps using loops, counters, and conditions; a terminating loop eventually reaches an end.
10. Pseudocode describes steps in readable language without requiring exact Kotlin syntax.
11. Algorithms define inputs, processing, outputs, and working variables to hold intermediate values.
12. A dry run traces an algorithm manually with sample values to check each step.

**Practice:** 5 sets × 10 questions = 50 questions. Question IDs ALG01–ALG50.

## Computer Hardware

1. CPU executes instructions and carries out arithmetic and control operations.
2. RAM holds temporary working data, while an SSD keeps files without power.
3. A PSU provides electrical power to components.
4. A motherboard connects the CPU, RAM and expansion parts; physical slots and memory specifications must be compatible.
5. A GPU specializes in graphics rendering and acceleration.
6. Fans, heatsinks and thermal interface materials help move heat away from components.
7. Keyboards, microphones and scanners are input devices; monitors, speakers and printers are output devices.
8. SSDs and HDDs store data without power. HDDs use spinning disks; SSDs use flash memory and may connect through SATA or NVMe/PCIe.
9. USB connects many peripherals, HDMI carries digital video/audio, and PCIe connects compatible expansion cards.
10. BIOS/UEFI firmware initializes hardware and starts the boot process before an operating system loads from storage.

**Practice:** 5 sets × 10 questions = 50 questions. Question IDs HWR01–HWR50.

