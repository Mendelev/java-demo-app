#!/usr/bin/env python3
"""
Script to find available VM SKUs across all publicly available Azure regions.
Focuses on B1s (cheapest) and D2 series (small general purpose) instances.
"""

import subprocess
import json
import sys

# Regions closest to Brazil (ordered by approximate distance)
REGIONS_NEAR_BRAZIL = [
    "brazilsouth",      # São Paulo, Brazil
    "chilecentral",     # Santiago, Chile  
    "eastus",           # Virginia, USA
    "eastus2",          # Virginia, USA
    "southcentralus",   # Texas, USA
    "centralus",        # Iowa, USA
    "northcentralus",   # Illinois, USA
    "westcentralus",    # Wyoming, USA
    "canadacentral",    # Toronto, Canada
    "canadaeast",       # Quebec, Canada
    "westus",           # California, USA
    "westus2",          # Washington, USA
    "westus3",          # Arizona, USA
    "mexicocentral",    # Mexico
]


def check_sku_availability(region: str, sku_pattern: str) -> dict:
    """Check if a SKU is available in a region with no restrictions."""
    try:
        cmd = [
            "az", "vm", "list-skus",
            "--location", region,
            "--size", sku_pattern,
            "--output", "json"
        ]
        result = subprocess.run(cmd, capture_output=True, text=True, timeout=120)
        
        if result.returncode != 0:
            return {"region": region, "sku": sku_pattern, "available": [], "error": result.stderr}
        
        skus = json.loads(result.stdout) if result.stdout else []
        
        # Filter for SKUs with no restrictions
        available = [
            sku["name"] for sku in skus 
            if sku.get("restrictions") == [] or sku.get("restrictions") is None
        ]
        
        return {"region": region, "sku": sku_pattern, "available": available, "error": None}
    
    except subprocess.TimeoutExpired:
        return {"region": region, "sku": sku_pattern, "available": [], "error": "Timeout"}
    except Exception as e:
        return {"region": region, "sku": sku_pattern, "available": [], "error": str(e)}


def search_regions_sequentially(sku_pattern: str, regions: list[str]) -> list[dict]:
    """Search for a SKU pattern across regions sequentially."""
    results = []
    
    print(f"\n🔍 Searching for {sku_pattern} across {len(regions)} regions...")
    print("-" * 60)
    
    for i, region in enumerate(regions, 1):
        sys.stdout.write(f"\r[{i}/{len(regions)}] Checking {region}...                    ")
        sys.stdout.flush()
        
        result = check_sku_availability(region, sku_pattern)
        available = result["available"]
        
        if available:
            print(f"\r✅ {region}: {', '.join(available[:5])}{'...' if len(available) > 5 else ''}                    ")
            results.append(result)
    
    print()  # New line after progress
    return results


def main():
    print("=" * 60)
    print("Azure VM SKU Availability Checker")
    print("Checking regions closest to Brazil")
    print("=" * 60)
    
    # Search for B1s instances in regions near Brazil
    print("\n" + "=" * 60)
    print("PHASE 1: Searching for Standard_B1s (cheapest option)")
    print("=" * 60)
    b1s_results = search_regions_sequentially("Standard_B1s", REGIONS_NEAR_BRAZIL)
    
    # Search for D2 series instances in regions near Brazil
    print("\n" + "=" * 60)
    print("PHASE 2: Searching for Standard_D2* (small general purpose)")
    print("=" * 60)
    d2_results = search_regions_sequentially("Standard_D2", REGIONS_NEAR_BRAZIL)
    
    # Summary
    print("\n" + "=" * 60)
    print("SUMMARY - Regions Near Brazil")
    print("=" * 60)
    
    print("\n📋 B1s Available Regions:")
    if b1s_results:
        for r in b1s_results:
            idx = REGIONS_NEAR_BRAZIL.index(r["region"]) + 1
            print(f"  {idx}. {r['region']}: {', '.join(r['available'])}")
    else:
        print("  ❌ No B1s available in any region near Brazil")
    
    print("\n📋 D2 Series Available Regions:")
    if d2_results:
        for r in d2_results:
            idx = REGIONS_NEAR_BRAZIL.index(r["region"]) + 1
            skus = r["available"][:5]
            more = f"... (+{len(r['available']) - 5} more)" if len(r["available"]) > 5 else ""
            print(f"  {idx}. {r['region']}: {', '.join(skus)} {more}")
    else:
        print("  ❌ No D2 series available in any region near Brazil")
    
    print("\n" + "=" * 60)
    print("RECOMMENDATIONS (ordered by distance from Brazil)")
    print("=" * 60)
    
    if b1s_results:
        best = b1s_results[0]
        print(f"\n🏆 Best B1s: {best['region']} - {best['available'][0]}")
        print(f"   VM Size: Standard_B1s (1 vCPU, 1 GB RAM) - ~$7/month")
    
    if d2_results:
        best = d2_results[0]
        # Prefer v5 or v4 versions, then s variants
        preferred = sorted(best["available"], key=lambda x: (
            "v5" not in x, "v4" not in x, "v3" not in x, "s_" not in x, x
        ))
        print(f"\n🏆 Best D2: {best['region']} - {preferred[0]}")
        print(f"   (2 vCPUs, varies RAM) - More expensive but more available")
    
    if not b1s_results and not d2_results:
        print("\n⚠️  No unrestricted VMs found. You may need to:")
        print("   1. Request quota increase from Azure")
        print("   2. Try a different VM family (A-series, F-series)")
        print("   3. Check if your subscription has regional restrictions")


if __name__ == "__main__":
    main()
