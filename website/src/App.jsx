import React, { useState } from 'react';
import { 
  Download, 
  Shield, 
  Radar, 
  Zap, 
  Crosshair, 
  Smartphone, 
  Cpu, 
  Layers, 
  MapPin, 
  CheckCircle, 
  Terminal, 
  Users, 
  Sparkles,
  ExternalLink,
  ChevronRight,
  Flame,
  Globe
} from 'lucide-react';

export default function App() {
  const [activeTab, setActiveTab] = useState('raids');

  // Releases download link (latest GitHub release APK)
  const apkDownloadUrl = "https://github.com/PUSHPAK-JAISWAL/CyberMaze-3D/releases/latest/download/CyberMaze-3D.apk";
  const repoUrl = "https://github.com/PUSHPAK-JAISWAL/CyberMaze-3D";

  const sectors = [
    { lvl: 1, name: "Neon Alley", diff: "Normal", color: "#00C882", badge: "Lasers" },
    { lvl: 2, name: "Iron Bastion", diff: "Hard", color: "#38EFAB", badge: "Mortars" },
    { lvl: 3, name: "Quantum Spire", diff: "Cyberpunk", color: "#00B4D8", badge: "Tesla Coils" },
    { lvl: 4, name: "Apex Citadel", diff: "Nightmare", color: "#9D4EDD", badge: "Stealth Mines" },
    { lvl: 5, name: "Sub-Zero Cryo-Vault", diff: "Extreme", color: "#00C882", badge: "Cryo Shield" },
    { lvl: 6, name: "Plasma Reactor Core", diff: "Ultra", color: "#38EFAB", badge: "Dual Mortars" },
    { lvl: 7, name: "Orbital Sky-Platform", diff: "Master", color: "#00B4D8", badge: "Ion Turrets" },
    { lvl: 8, name: "Zero-Day Dark Citadel", diff: "Legendary", color: "#9D4EDD", badge: "Quantum Boss" },
    { lvl: 9, name: "Hyperion Grid Fortress", diff: "Ascendant", color: "#FFB703", badge: "Grid Overload" },
    { lvl: 10, name: "Chrono Warp Facility", diff: "Titan", color: "#FF5252", badge: "Time Stun" },
    { lvl: 11, name: "Singularity Core", diff: "Cosmic", color: "#9D4EDD", badge: "Black Hole Trap" },
    { lvl: 12, name: "Cyber Overlord Nexus", diff: "God Tier", color: "#00C882", badge: "Final Nexus" },
  ];

  const features = [
    {
      icon: <Radar className="w-8 h-8 text-[#00C882]" />,
      title: "Real-World Geo-Radar",
      desc: "Connects to physical mobile accelerometer & pedometer sensors. Walking outside awards 2× Neon Bits and decrypts rare outdoor Darknet caches."
    },
    {
      icon: <Users className="w-8 h-8 text-[#FF5252]" />,
      title: "Pokémon GO Proximity Battles",
      desc: "When other players running CyberMaze are physically nearby, their base pings your radar in red. Infiltrate their custom fortresses in live PvP raids!"
    },
    {
      icon: <Shield className="w-8 h-8 text-[#00B4D8]" />,
      title: "8×8 Fortress Maze Builder",
      desc: "Design intricate neon wall mazes to funnel enemy squads directly into laser, tesla, and mortar kill zones. Protect your Quantum Core at all costs."
    },
    {
      icon: <Cpu className="w-8 h-8 text-[#9D4EDD]" />,
      title: "Multi-Provider BYOK AI",
      desc: "Bring Your Own Key support for Google Gemini, Groq (Llama-3), OpenRouter, and OpenAI. Instant tactical recon briefings and base defense security audits."
    },
    {
      icon: <Zap className="w-8 h-8 text-[#FFB703]" />,
      title: "In-App Seamless APK Updates",
      desc: "Direct background update pipeline powered by GitHub Releases. Automatically notifies you of new versions and installs with 1 tap."
    },
    {
      icon: <Layers className="w-8 h-8 text-[#38EFAB]" />,
      title: "SQLite Room Offline Engine",
      desc: "Zero cloud lag. Completely offline-capable with local Room database persistence for saved mazes, upgrade decks, and movement telemetry."
    }
  ];

  const troops = [
    { name: "Byte Brawler", cost: "3⚡", role: "Armored Tank", desc: "High HP juggernaut that smashes through walls and targets defenses first." },
    { name: "Glitch Sprinter", cost: "2⚡", role: "Speed Infiltrator", desc: "Ignores mines and traps, rushing straight to the enemy Quantum Core." },
    { name: "EMP Specialist", cost: "4⚡", role: "Ranged Hacker", desc: "Fires electrical arc bolts that stun and disable enemy turrets." },
    { name: "Phantom Drone", cost: "5⚡", role: "Aerial Hover", desc: "Glides effortlessly over all ground walls, barriers, and laser gates." },
  ];

  return (
    <div className="min-h-screen bg-[#071712] text-[#E8FDF5] font-sans cyber-grid-bg">
      {/* Header / Navbar */}
      <nav className="sticky top-0 z-50 backdrop-blur-md bg-[#071712]/90 border-b border-[#205540] px-4 md:px-8 py-4">
        <div className="max-w-7xl mx-auto flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <div className="w-10 h-10 rounded-lg bg-[#00C882] flex items-center justify-center font-mono font-bold text-[#003822] text-xl cyber-glow-mint">
              CM
            </div>
            <div>
              <span className="font-mono font-extrabold text-lg text-[#E8FDF5] tracking-wider">CYBERMAZE 3D</span>
              <span className="ml-2 text-xs font-mono px-2 py-0.5 rounded bg-[#113326] text-[#00C882] border border-[#205540]">v1.2.0</span>
            </div>
          </div>

          <div className="hidden md:flex items-center space-x-6 font-mono text-sm">
            <a href="#features" className="hover:text-[#00C882] transition-colors">FEATURES</a>
            <a href="#levels" className="hover:text-[#00C882] transition-colors">12 SECTORS</a>
            <a href="#squad" className="hover:text-[#00C882] transition-colors">TROOP DECK</a>
            <a href="#outdoor" className="hover:text-[#00C882] transition-colors">OUTDOOR RADAR</a>
          </div>

          <div className="flex items-center space-x-3">
            <a 
              href={apkDownloadUrl}
              className="flex items-center space-x-2 bg-[#00C882] hover:bg-[#38EFAB] text-[#003822] px-4 py-2 rounded-lg font-mono font-bold text-sm transition-all cyber-glow-mint"
            >
              <Download className="w-4 h-4" />
              <span>DOWNLOAD APK</span>
            </a>
          </div>
        </div>
      </nav>

      {/* Hero Section */}
      <section className="relative px-4 md:px-8 pt-16 pb-20 max-w-7xl mx-auto text-center">
        <div className="inline-flex items-center space-x-2 px-3 py-1.5 rounded-full bg-[#113326] border border-[#00C882]/40 text-[#00C882] font-mono text-xs mb-8">
          <Sparkles className="w-3.5 h-3.5 text-[#00C882]" />
          <span>TACTICAL CYBER FORTRESS WARFARE + REAL-WORLD GEO-RADAR</span>
        </div>

        <h1 className="text-4xl md:text-6xl lg:text-7xl font-extrabold font-mono tracking-tight max-w-5xl mx-auto leading-tight md:leading-none">
          BREACH THE <span className="text-transparent bg-clip-text bg-gradient-to-r from-[#00C882] via-[#00B4D8] to-[#38EFAB]">CYBER MAZE</span>.<br />
          DEFEND YOUR <span className="text-[#00C882]">CORE</span>.
        </h1>

        <p className="mt-6 text-lg md:text-xl text-[#A1CFC0] max-w-3xl mx-auto leading-relaxed">
          The ultimate Android cyber strategy battle game combining <strong>real-time squad raids</strong>, 
          <strong> 8×8 neon maze defense</strong>, and <strong>physical outdoor movement</strong> where walking in the real world 
          powers your army and detects nearby players' fortresses!
        </p>

        {/* Hero Actions */}
        <div className="mt-10 flex flex-col sm:flex-row items-center justify-center gap-4">
          <a
            href={apkDownloadUrl}
            className="w-full sm:w-auto flex items-center justify-center space-x-3 bg-[#00C882] hover:bg-[#38EFAB] text-[#003822] px-8 py-4 rounded-xl font-mono font-extrabold text-base transition-all cyber-glow-mint"
          >
            <Download className="w-5 h-5" />
            <span>DOWNLOAD ANDROID APK (v1.2.0)</span>
          </a>

          <a
            href={repoUrl}
            target="_blank"
            rel="noopener noreferrer"
            className="w-full sm:w-auto flex items-center justify-center space-x-2 bg-[#113326] hover:bg-[#163E2F] text-[#E8FDF5] border border-[#205540] hover:border-[#00C882] px-6 py-4 rounded-xl font-mono text-sm transition-all"
          >
            <ExternalLink className="w-4 h-4 text-[#00B4D8]" />
            <span>VIEW ON GITHUB</span>
          </a>
        </div>

        {/* Quick Highlights Badge Row */}
        <div className="mt-12 grid grid-cols-2 sm:grid-cols-4 gap-4 max-w-4xl mx-auto font-mono text-xs">
          <div className="p-3 rounded-lg bg-[#0C241B] border border-[#205540]">
            <div className="text-[#00C882] font-bold text-lg">12 SECTORS</div>
            <div className="text-[#A1CFC0]">Procedural Campaign</div>
          </div>
          <div className="p-3 rounded-lg bg-[#0C241B] border border-[#205540]">
            <div className="text-[#00B4D8] font-bold text-lg">GEO-RADAR</div>
            <div className="text-[#A1CFC0]">Physical Step Tracking</div>
          </div>
          <div className="p-3 rounded-lg bg-[#0C241B] border border-[#205540]">
            <div className="text-[#FF5252] font-bold text-lg">PROXIMITY PVP</div>
            <div className="text-[#A1CFC0]">Nearby Player Raids</div>
          </div>
          <div className="p-3 rounded-lg bg-[#0C241B] border border-[#205540]">
            <div className="text-[#9D4EDD] font-bold text-lg">BYOK AI</div>
            <div className="text-[#A1CFC0]">Gemini / Groq / OpenAI</div>
          </div>
        </div>
      </section>

      {/* 12 Sectors Showcase Section */}
      <section id="levels" className="px-4 md:px-8 py-16 bg-[#091F18]/80 border-y border-[#205540]">
        <div className="max-w-7xl mx-auto">
          <div className="flex flex-col md:flex-row items-start md:items-end justify-between mb-10">
            <div>
              <div className="text-[#00C882] font-mono font-bold text-xs tracking-wider">EXPANDED CAMPAIGN</div>
              <h2 className="text-3xl md:text-4xl font-extrabold font-mono mt-1">12 PROGRESSIVE SECTOR LEVELS</h2>
            </div>
            <p className="text-[#A1CFC0] text-sm md:max-w-md mt-2 md:mt-0 font-mono">
              Defeat each rival syndicate outpost to unlock higher-tier defensive hazards, stronger Quantum Cores, and epic loot.
            </p>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
            {sectors.map((sec) => (
              <div 
                key={sec.lvl}
                className="p-4 rounded-xl bg-[#071912] border border-[#205540] hover:border-[#00C882] transition-all hover:-translate-y-1"
              >
                <div className="flex items-center justify-between">
                  <span className="font-mono text-xs px-2 py-0.5 rounded bg-[#00C882]/10 text-[#00C882] border border-[#00C882]/30 font-bold">
                    SECTOR {sec.lvl}
                  </span>
                  <span className="font-mono text-xs text-[#FFB703] font-semibold">{sec.diff}</span>
                </div>
                <h3 className="font-mono font-bold text-base mt-2 text-[#E8FDF5]">{sec.name}</h3>
                <div className="mt-3 flex items-center justify-between text-xs font-mono text-[#A1CFC0]">
                  <span>Hazard:</span>
                  <span className="text-[#00B4D8] font-semibold">{sec.badge}</span>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Key Gameplay Pillars */}
      <section id="features" className="px-4 md:px-8 py-20 max-w-7xl mx-auto">
        <div className="text-center max-w-3xl mx-auto mb-16">
          <div className="text-[#00C882] font-mono font-bold text-xs tracking-wider">CORE PILLARS</div>
          <h2 className="text-3xl md:text-4xl font-extrabold font-mono mt-1">ENGINEERED FOR TACTICAL WARFARE</h2>
          <p className="mt-4 text-[#A1CFC0]">
            From hardware sensors to multi-provider LLMs, CyberMaze 3D is crafted with zero-compromise modern Android tech.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {features.map((feat, idx) => (
            <div 
              key={idx}
              className="p-6 rounded-2xl bg-[#0C241B] border border-[#205540] hover:border-[#00C882]/70 transition-all"
            >
              <div className="p-3 rounded-xl bg-[#071712] w-fit border border-[#205540] mb-4">
                {feat.icon}
              </div>
              <h3 className="text-xl font-mono font-bold text-[#E8FDF5] mb-2">{feat.title}</h3>
              <p className="text-sm text-[#A1CFC0] leading-relaxed">{feat.desc}</p>
            </div>
          ))}
        </div>
      </section>

      {/* Pokémon GO Style Proximity PvP Showcase */}
      <section id="outdoor" className="px-4 md:px-8 py-16 bg-[#160B0F]/90 border-y border-[#FF5252]/30">
        <div className="max-w-7xl mx-auto flex flex-col lg:flex-row items-center gap-12">
          <div className="lg:w-1/2">
            <div className="inline-flex items-center space-x-2 px-3 py-1 rounded-full bg-[#FF5252]/10 border border-[#FF5252]/40 text-[#FF5252] font-mono text-xs mb-4">
              <Crosshair className="w-3.5 h-3.5" />
              <span>POKÉMON GO STYLE PROXIMITY WARFARE</span>
            </div>
            <h2 className="text-3xl md:text-4xl font-extrabold font-mono text-[#E8FDF5] leading-tight">
              DETECT & RAID NEARBY PLAYERS IN THE REAL WORLD
            </h2>
            <p className="mt-4 text-[#A1CFC0] text-sm md:text-base leading-relaxed">
              When other players running CyberMaze 3D enter your physical radius, their custom designed base defenses 
              ping your 360° Circular Radar in glowing crimson blips!
            </p>
            <ul className="mt-6 space-y-3 font-mono text-xs md:text-sm text-[#E8FDF5]">
              <li className="flex items-center space-x-3">
                <CheckCircle className="w-5 h-5 text-[#FF5252] flex-shrink-0" />
                <span>See architect callsigns, league trophy rank, and exact meter distance</span>
              </li>
              <li className="flex items-center space-x-3">
                <CheckCircle className="w-5 h-5 text-[#FF5252] flex-shrink-0" />
                <span>One-tap "INFILTRATE & ATTACK THIS PLAYER'S BASE" live raids</span>
              </li>
              <li className="flex items-center space-x-3">
                <CheckCircle className="w-5 h-5 text-[#FF5252] flex-shrink-0" />
                <span>Destroy their Quantum Core to plunder their stored Neon Bits & Trophies</span>
              </li>
            </ul>
          </div>

          <div className="lg:w-1/2 w-full">
            <div className="p-6 rounded-2xl bg-[#071712] border border-[#FF5252]/40 cyber-glow-red">
              <div className="flex items-center justify-between pb-4 border-b border-[#205540]">
                <div className="flex items-center space-x-2">
                  <div className="w-3 h-3 rounded-full bg-[#FF5252] animate-ping" />
                  <span className="font-mono text-xs font-bold text-[#FF5252]">RADAR TARGET DETECTED (42m)</span>
                </div>
                <span className="font-mono text-xs text-[#00C882]">LOOT: +350⚡</span>
              </div>
              <div className="mt-4">
                <div className="font-mono font-bold text-lg text-white">Kira_Zero</div>
                <div className="text-xs font-mono text-[#A1CFC0]">Apex Syndicate • 480 🏆 • 10 Fortified Defenses</div>
              </div>
              <div className="mt-6 p-3 rounded-lg bg-[#113326] border border-[#205540] flex items-center justify-between text-xs font-mono">
                <span className="text-[#38EFAB]">Core Server HP: 1,500</span>
                <span className="text-[#FFB703]">Laser Turrets: 3</span>
                <span className="text-[#00B4D8]">Tesla Pylons: 2</span>
              </div>
              <button 
                onClick={() => alert("Install CyberMaze 3D APK on your device to raid nearby players!")}
                className="mt-4 w-full bg-[#FF5252] hover:bg-[#FF6B6B] text-white py-3 rounded-xl font-mono font-bold text-xs uppercase tracking-wider flex items-center justify-center space-x-2 transition-all"
              >
                <Crosshair className="w-4 h-4" />
                <span>INFILTRATE THIS PLAYER'S BASE</span>
              </button>
            </div>
          </div>
        </div>
      </section>

      {/* Troop Squad Deck */}
      <section id="squad" className="px-4 md:px-8 py-20 max-w-7xl mx-auto">
        <div className="text-center max-w-3xl mx-auto mb-16">
          <div className="text-[#00C882] font-mono font-bold text-xs tracking-wider">TACTICAL SQUAD</div>
          <h2 className="text-3xl md:text-4xl font-extrabold font-mono mt-1">SPECIALIZED CYBER OPERATIVES</h2>
          <p className="mt-4 text-[#A1CFC0]">
            Assemble your 4-card assault deck to outmaneuver complex turret choke points and wall mazes.
          </p>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          {troops.map((troop, idx) => (
            <div 
              key={idx}
              className="p-5 rounded-2xl bg-[#0C241B] border border-[#205540] hover:border-[#00C882] transition-all flex flex-col justify-between"
            >
              <div>
                <div className="flex items-center justify-between mb-3">
                  <span className="font-mono text-xs font-extrabold text-[#00C882] px-2 py-1 rounded bg-[#00C882]/10 border border-[#00C882]/30">
                    COST: {troop.cost}
                  </span>
                  <span className="font-mono text-xs text-[#00B4D8]">{troop.role}</span>
                </div>
                <h3 className="font-mono font-bold text-lg text-white mb-2">{troop.name}</h3>
                <p className="text-xs text-[#A1CFC0] leading-relaxed">{troop.desc}</p>
              </div>
              <div className="mt-6 pt-3 border-t border-[#205540] flex items-center justify-between text-xs font-mono text-[#38EFAB]">
                <span>Upgradable</span>
                <span>Max Lvl 10</span>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* CTA / Download Banner */}
      <section className="px-4 md:px-8 py-20 bg-gradient-to-b from-[#0C241B] to-[#071712] border-t border-[#205540] text-center">
        <div className="max-w-4xl mx-auto">
          <Smartphone className="w-12 h-12 text-[#00C882] mx-auto mb-6" />
          <h2 className="text-3xl md:text-5xl font-extrabold font-mono tracking-tight">
            READY TO INFILTRATE THE SYNDICATE?
          </h2>
          <p className="mt-4 text-base md:text-lg text-[#A1CFC0] max-w-2xl mx-auto">
            Download the Android APK now. Built deterministically with unified signing, 
            instant in-app auto updates, and offline SQLite Room persistence.
          </p>
          <div className="mt-8 flex flex-col sm:flex-row items-center justify-center gap-4">
            <a
              href={apkDownloadUrl}
              className="w-full sm:w-auto flex items-center justify-center space-x-3 bg-[#00C882] hover:bg-[#38EFAB] text-[#003822] px-8 py-4 rounded-xl font-mono font-extrabold text-base transition-all cyber-glow-mint"
            >
              <Download className="w-5 h-5" />
              <span>DOWNLOAD CYBERMAZE-3D.APK</span>
            </a>
            <a
              href={repoUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="w-full sm:w-auto flex items-center justify-center space-x-2 bg-[#113326] hover:bg-[#163E2F] text-[#E8FDF5] border border-[#205540] px-6 py-4 rounded-xl font-mono text-sm transition-all"
            >
              <ExternalLink className="w-4 h-4 text-[#00B4D8]" />
              <span>SOURCE CODE ON GITHUB</span>
            </a>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="px-4 md:px-8 py-8 border-t border-[#205540] text-center font-mono text-xs text-[#A1CFC0]">
        <div className="max-w-7xl mx-auto flex flex-col sm:flex-row items-center justify-between gap-4">
          <div>
            © 2026 CYBERMAZE 3D. Tactical Cyber Strategy & Sensor Gaming.
          </div>
          <div className="flex items-center space-x-4">
            <a href={repoUrl} className="hover:text-[#00C882]">GitHub Repository</a>
            <span>•</span>
            <a href={`${repoUrl}/releases`} className="hover:text-[#00C882]">Releases & APKs</a>
            <span>•</span>
            <a href={`${repoUrl}/blob/main/LICENSE`} className="hover:text-[#00C882]">MIT License</a>
          </div>
        </div>
      </footer>
    </div>
  );
}
