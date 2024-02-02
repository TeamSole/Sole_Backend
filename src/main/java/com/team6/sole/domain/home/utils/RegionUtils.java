package com.team6.sole.domain.home.utils;

import com.team6.sole.domain.home.model.Region;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RegionUtils {
    // 지역명 필터링
    public static String makeShortenAddress(String address) {
        String[] addressArr = address.split(" ");
        String shortenAddress = "";
        switch (addressArr[0]) {
            case "서울특별시":
            case "부산광역시":
            case "대구광역시":
            case "인천광역시":
            case "대전광역시":
            case "울산광역시":
            case "광주광역시":
            case "세종특별자치시":
            case "제주특별자치도":
            case "경기도":
            case "강원도":
                shortenAddress = addressArr[0].substring(0, 2) + " " + addressArr[1];
                break;
            case "충청북도":
            case "충청남도":
            case "전라북도":
            case "전라남도":
            case "경상북도":
            case "경상남도":
                shortenAddress = addressArr[0].charAt(0) + addressArr[0].charAt(2) + " " + addressArr[1];
                break;
            default:
                shortenAddress = addressArr[0];
                break;
        }

        return shortenAddress;
    }

    // 지역명(String) -> Region(Enum)
    public static Region makeRegion(String shortenAddress) {
        Region region = null;

        if (shortenAddress.startsWith("서울")) {
            if (shortenAddress.contains("강남")) {
                return Region.S01;
            } else if (shortenAddress.contains("강동")) {
                return Region.S02;
            } else if (shortenAddress.contains("강북")) {
                return Region.S03;
            } else if (shortenAddress.contains("강서")) {
                return Region.S04;
            } else if (shortenAddress.contains("관악")) {
                return Region.S05;
            } else if (shortenAddress.contains("광진")) {
                return Region.S06;
            } else if (shortenAddress.contains("구로")) {
                return Region.S07;
            } else if (shortenAddress.contains("금천")) {
                return Region.S08;
            } else if (shortenAddress.contains("노원")) {
                return Region.S09;
            } else if (shortenAddress.contains("도봉")) {
                return Region.S10;
            } else if (shortenAddress.contains("동대문")) {
                return Region.S11;
            } else if (shortenAddress.contains("동작")) {
                return Region.S12;
            } else if (shortenAddress.contains("마포")) {
                return Region.S13;
            } else if (shortenAddress.contains("서대문")) {
                return Region.S14;
            } else if (shortenAddress.contains("서초")) {
                return Region.S15;
            } else if (shortenAddress.contains("성동")) {
                return Region.S16;
            } else if (shortenAddress.contains("성북")) {
                return Region.S17;
            } else if (shortenAddress.contains("송파")) {
                return Region.S18;
            } else if (shortenAddress.contains("양천")) {
                return Region.S19;
            } else if (shortenAddress.contains("영등포")) {
                return Region.S20;
            } else if (shortenAddress.contains("용산")) {
                return Region.S21;
            } else if (shortenAddress.contains("은평")) {
                return Region.S22;
            } else if (shortenAddress.contains("종로")) {
                return Region.S23;
            } else if (shortenAddress.contains("중구")) {
                return Region.S24;
            } else {
                return Region.S25;
            }
        } else if (shortenAddress.startsWith("경기")) {
            if (shortenAddress.contains("가평")) {
                return Region.K01;
            } else if (shortenAddress.contains("고양")) {
                return Region.K02;
            } else if (shortenAddress.contains("과천")) {
                return Region.K03;
            } else if (shortenAddress.contains("광명")) {
                return Region.K04;
            } else if (shortenAddress.contains("광주")) {
                return Region.K05;
            } else if (shortenAddress.contains("구리")) {
                return Region.K06;
            } else if (shortenAddress.contains("군포")) {
                return Region.K07;
            } else if (shortenAddress.contains("김포")) {
                return Region.K08;
            } else if (shortenAddress.contains("남양주")) {
                return Region.K09;
            } else if (shortenAddress.contains("동두천")) {
                return Region.K10;
            } else if (shortenAddress.contains("부천")) {
                return Region.K11;
            } else if (shortenAddress.contains("성남")) {
                return Region.K12;
            } else if (shortenAddress.contains("수원")) {
                return Region.K13;
            } else if (shortenAddress.contains("시흥")) {
                return Region.K14;
            } else if (shortenAddress.contains("안산")) {
                return Region.K15;
            } else if (shortenAddress.contains("안성")) {
                return Region.K16;
            } else if (shortenAddress.contains("안양")) {
                return Region.K17;
            } else if (shortenAddress.contains("양주")) {
                return Region.K18;
            } else if (shortenAddress.contains("양평")) {
                return Region.K19;
            } else if (shortenAddress.contains("여주")) {
                return Region.K20;
            } else if (shortenAddress.contains("연천")) {
                return Region.K21;
            } else if (shortenAddress.contains("오산")) {
                return Region.K22;
            } else if (shortenAddress.contains("용인")) {
                return Region.K23;
            } else if (shortenAddress.contains("의왕")) {
                return Region.K24;
            } else if (shortenAddress.contains("의정부")) {
                return Region.K25;
            } else if (shortenAddress.contains("이천")) {
                return Region.K26;
            } else if (shortenAddress.contains("파주")) {
                return Region.K27;
            } else if (shortenAddress.contains("평택")) {
                return Region.K28;
            } else if (shortenAddress.contains("포천")) {
                return Region.K29;
            } else if (shortenAddress.contains("하남")) {
                return Region.K30;
            } else if (shortenAddress.contains("화성")) {
                return Region.K31;
            }
        } else if (shortenAddress.startsWith("인천")) {
            if (shortenAddress.contains("강화")) {
                return Region.I01;
            } else if (shortenAddress.contains("계양")) {
                return Region.I02;
            } else if (shortenAddress.contains("남동")) {
                return Region.I03;
            } else if (shortenAddress.contains("동구")) {
                return Region.I04;
            } else if (shortenAddress.contains("미추홀")) {
                return Region.I05;
            } else if (shortenAddress.contains("부평")) {
                return Region.I06;
            } else if (shortenAddress.contains("서구")) {
                return Region.I07;
            } else if (shortenAddress.contains("연수")) {
                return Region.I08;
            } else if (shortenAddress.contains("옹진")) {
                return Region.I09;
            } else if (shortenAddress.contains("중구")) {
                return Region.I10;
            }
        } else if (shortenAddress.startsWith("강원")) {
            if (shortenAddress.contains("강릉")) {
                return Region.KW01;
            } else if (shortenAddress.contains("고성")) {
                return Region.KW02;
            } else if (shortenAddress.contains("동해")) {
                return Region.KW03;
            } else if (shortenAddress.contains("삼척")) {
                return Region.KW04;
            } else if (shortenAddress.contains("속초")) {
                return Region.KW05;
            } else if (shortenAddress.contains("양구")) {
                return Region.KW06;
            } else if (shortenAddress.contains("양양")) {
                return Region.KW07;
            } else if (shortenAddress.contains("영월")) {
                return Region.KW08;
            } else if (shortenAddress.contains("원주")) {
                return Region.KW09;
            } else if (shortenAddress.contains("인제")) {
                return Region.KW10;
            } else if (shortenAddress.contains("정선")) {
                return Region.KW11;
            } else if (shortenAddress.contains("철원")) {
                return Region.KW12;
            } else if (shortenAddress.contains("춘천")) {
                return Region.KW13;
            } else if (shortenAddress.contains("태백")) {
                return Region.KW14;
            } else if (shortenAddress.contains("평창")) {
                return Region.KW15;
            } else if (shortenAddress.contains("홍천")) {
                return Region.KW16;
            } else if (shortenAddress.contains("화천")) {
                return Region.KW17;
            } else if (shortenAddress.contains("횡성")) {
                return Region.KW18;
            }
        } else if (shortenAddress.startsWith("충북")) {
            if (shortenAddress.contains("괴산")) {
                return Region.CB01;
            } else if (shortenAddress.contains("단양")) {
                return Region.CB02;
            } else if (shortenAddress.contains("보은")) {
                return Region.CB03;
            } else if (shortenAddress.contains("영동")) {
                return Region.CB04;
            } else if (shortenAddress.contains("옥천")) {
                return Region.CB05;
            } else if (shortenAddress.contains("음성")) {
                return Region.CB06;
            } else if (shortenAddress.contains("제천")) {
                return Region.CB07;
            } else if (shortenAddress.contains("증평")) {
                return Region.CB08;
            } else if (shortenAddress.contains("진천")) {
                return Region.CB09;
            } else if (shortenAddress.contains("청주")) {
                return Region.CB10;
            } else if (shortenAddress.contains("충주")) {
                return Region.CB11;
            }
        } else if (shortenAddress.startsWith("충남")) {
            if (shortenAddress.contains("계룡")) {
                return Region.CN01;
            } else if (shortenAddress.contains("공주")) {
                return Region.CN02;
            } else if (shortenAddress.contains("금산")) {
                return Region.CN03;
            } else if (shortenAddress.contains("논산")) {
                return Region.CN04;
            } else if (shortenAddress.contains("당진")) {
                return Region.CN05;
            } else if (shortenAddress.contains("보령")) {
                return Region.CN06;
            } else if (shortenAddress.contains("부여")) {
                return Region.CN07;
            } else if (shortenAddress.contains("서산")) {
                return Region.CN08;
            } else if (shortenAddress.contains("서천")) {
                return Region.CN09;
            } else if (shortenAddress.contains("아산")) {
                return Region.CN10;
            } else if (shortenAddress.contains("예산")) {
                return Region.CN11;
            } else if (shortenAddress.contains("천안")) {
                return Region.CN12;
            } else if (shortenAddress.contains("청양")) {
                return Region.CN13;
            } else if (shortenAddress.contains("태안")) {
                return Region.CN14;
            } else if (shortenAddress.contains("홍성")) {
                return Region.CN15;
            }
        } else if (shortenAddress.startsWith("대전")) {
            if (shortenAddress.contains("대덕")) {
                return Region.DJ01;
            } else if (shortenAddress.contains("동구")) {
                return Region.DJ02;
            } else if (shortenAddress.contains("서구")) {
                return Region.DJ03;
            } else if (shortenAddress.contains("유성")) {
                return Region.DJ04;
            } else if (shortenAddress.contains("중구")) {
                return Region.DJ05;
            }
        } else if (shortenAddress.startsWith("세종")) {
            return Region.SGG;
        } else if (shortenAddress.startsWith("경북")) {
            if (shortenAddress.contains("경산")) {
                return Region.GB01;
            } else if (shortenAddress.contains("경주")) {
                return Region.GB02;
            } else if (shortenAddress.contains("고령")) {
                return Region.GB03;
            } else if (shortenAddress.contains("구미")) {
                return Region.GB04;
            } else if (shortenAddress.contains("군위")) {
                return Region.GB05;
            } else if (shortenAddress.contains("김천")) {
                return Region.GB06;
            } else if (shortenAddress.contains("문경")) {
                return Region.GB07;
            } else if (shortenAddress.contains("봉화")) {
                return Region.GB08;
            } else if (shortenAddress.contains("상주")) {
                return Region.GB09;
            } else if (shortenAddress.contains("성주")) {
                return Region.GB10;
            } else if (shortenAddress.contains("안동")) {
                return Region.GB11;
            } else if (shortenAddress.contains("영덕")) {
                return Region.GB12;
            } else if (shortenAddress.contains("영양")) {
                return Region.GB13;
            } else if (shortenAddress.contains("영주")) {
                return Region.GB14;
            } else if (shortenAddress.contains("영천")) {
                return Region.GB15;
            } else if (shortenAddress.contains("예천")) {
                return Region.GB16;
            } else if (shortenAddress.contains("울릉")) {
                return Region.GB17;
            } else if (shortenAddress.contains("울진")) {
                return Region.GB18;
            } else if (shortenAddress.contains("의성")) {
                return Region.GB19;
            } else if (shortenAddress.contains("청도")) {
                return Region.GB20;
            } else if (shortenAddress.contains("청송")) {
                return Region.GB21;
            } else if (shortenAddress.contains("칠곡")) {
                return Region.GB22;
            } else if (shortenAddress.contains("포항")) {
                return Region.GB23;
            }
        } else if (shortenAddress.startsWith("경남")) {
            if (shortenAddress.contains("거제")) {
                return Region.GN01;
            } else if (shortenAddress.contains("거창")) {
                return Region.GN02;
            } else if (shortenAddress.contains("고성")) {
                return Region.GN03;
            } else if (shortenAddress.contains("김해")) {
                return Region.GN04;
            } else if (shortenAddress.contains("남해")) {
                return Region.GN05;
            } else if (shortenAddress.contains("밀양")) {
                return Region.GN06;
            } else if (shortenAddress.contains("사천")) {
                return Region.GN07;
            } else if (shortenAddress.contains("산청")) {
                return Region.GN08;
            } else if (shortenAddress.contains("양산")) {
                return Region.GN09;
            } else if (shortenAddress.contains("의령")) {
                return Region.GN10;
            } else if (shortenAddress.contains("진주")) {
                return Region.GN11;
            } else if (shortenAddress.contains("창녕")) {
                return Region.GN12;
            } else if (shortenAddress.contains("창원")) {
                return Region.GN13;
            } else if (shortenAddress.contains("통영")) {
                return Region.GN14;
            } else if (shortenAddress.contains("하동")) {
                return Region.GN15;
            } else if (shortenAddress.contains("함안")) {
                return Region.GN16;
            } else if (shortenAddress.contains("함양")) {
                return Region.GN17;
            } else if (shortenAddress.contains("합천")) {
                return Region.GN18;
            }
        } else if (shortenAddress.startsWith("대구")) {
            if (shortenAddress.contains("남구")) {
                return Region.D01;
            } else if (shortenAddress.contains("달서")) {
                return Region.D02;
            } else if (shortenAddress.contains("달성")) {
                return Region.D03;
            } else if (shortenAddress.contains("동구")) {
                return Region.D04;
            } else if (shortenAddress.contains("북구")) {
                return Region.D05;
            } else if (shortenAddress.contains("서구")) {
                return Region.D06;
            } else if (shortenAddress.contains("수성")) {
                return Region.D07;
            } else if (shortenAddress.contains("중구")) {
                return Region.D08;
            }
        } else if (shortenAddress.startsWith("울산")) {
            if (shortenAddress.contains("남구")) {
                return Region.U01;
            } else if (shortenAddress.contains("동구")) {
                return Region.U02;
            } else if (shortenAddress.contains("북구")) {
                return Region.U03;
            } else if (shortenAddress.contains("울주")) {
                return Region.U04;
            } else if (shortenAddress.contains("중구")) {
                return Region.U05;
            }
        } else if (shortenAddress.startsWith("부산")) {
            if (shortenAddress.contains("강서")) {
                return Region.B01;
            } else if (shortenAddress.contains("금정")) {
                return Region.B02;
            } else if (shortenAddress.contains("기장")) {
                return Region.B03;
            } else if (shortenAddress.contains("남구")) {
                return Region.B04;
            } else if (shortenAddress.contains("동구")) {
                return Region.B05;
            } else if (shortenAddress.contains("동래")) {
                return Region.B06;
            } else if (shortenAddress.contains("부산진")) {
                return Region.B07;
            } else if (shortenAddress.contains("북구")) {
                return Region.B08;
            } else if (shortenAddress.contains("사상")) {
                return Region.B09;
            } else if (shortenAddress.contains("사하")) {
                return Region.B10;
            } else if (shortenAddress.contains("서구")) {
                return Region.B11;
            } else if (shortenAddress.contains("수영")) {
                return Region.B12;
            } else if (shortenAddress.contains("연제")) {
                return Region.B13;
            } else if (shortenAddress.contains("영도")) {
                return Region.B14;
            } else if (shortenAddress.contains("중구")) {
                return Region.B15;
            } else if (shortenAddress.contains("해운대")) {
                return Region.B16;
            }
        } else if (shortenAddress.startsWith("전북")) {
            if (shortenAddress.contains("고창")) {
                return Region.JB01;
            } else if (shortenAddress.contains("군산")) {
                return Region.JB02;
            } else if (shortenAddress.contains("김제")) {
                return Region.JB03;
            } else if (shortenAddress.contains("남원")) {
                return Region.JB04;
            } else if (shortenAddress.contains("무주")) {
                return Region.JB05;
            } else if (shortenAddress.contains("부안")) {
                return Region.JB06;
            } else if (shortenAddress.contains("순창")) {
                return Region.JB07;
            } else if (shortenAddress.contains("완주")) {
                return Region.JB08;
            } else if (shortenAddress.contains("익산")) {
                return Region.JB09;
            } else if (shortenAddress.contains("임실")) {
                return Region.JB10;
            } else if (shortenAddress.contains("장수")) {
                return Region.JB11;
            } else if (shortenAddress.contains("전주")) {
                return Region.JB12;
            } else if (shortenAddress.contains("정읍")) {
                return Region.JB13;
            } else if (shortenAddress.contains("진안")) {
                return Region.JB14;
            }
        } else if (shortenAddress.startsWith("전남")) {
            if (shortenAddress.contains("강진")) {
                return Region.JN01;
            } else if (shortenAddress.contains("고흥")) {
                return Region.JN02;
            } else if (shortenAddress.contains("곡성")) {
                return Region.JN03;
            } else if (shortenAddress.contains("광양")) {
                return Region.JN04;
            } else if (shortenAddress.contains("구례")) {
                return Region.JN05;
            } else if (shortenAddress.contains("나주")) {
                return Region.JN06;
            } else if (shortenAddress.contains("담양")) {
                return Region.JN07;
            } else if (shortenAddress.contains("목포")) {
                return Region.JN08;
            } else if (shortenAddress.contains("무안")) {
                return Region.JN09;
            } else if (shortenAddress.contains("보성")) {
                return Region.JN10;
            } else if (shortenAddress.contains("순천")) {
                return Region.JN11;
            } else if (shortenAddress.contains("신안")) {
                return Region.JN12;
            } else if (shortenAddress.contains("여수")) {
                return Region.JN13;
            } else if (shortenAddress.contains("영광")) {
                return Region.JN14;
            } else if (shortenAddress.contains("영암")) {
                return Region.JN15;
            } else if (shortenAddress.contains("완도")) {
                return Region.JN16;
            } else if (shortenAddress.contains("장성")) {
                return Region.JN17;
            } else if (shortenAddress.contains("장흥")) {
                return Region.JN18;
            } else if (shortenAddress.contains("진도")) {
                return Region.JN19;
            } else if (shortenAddress.contains("함평")) {
                return Region.JN20;
            } else if (shortenAddress.contains("해남")) {
                return Region.JN21;
            } else if (shortenAddress.contains("화순")) {
                return Region.JN22;
            }
        } else if (shortenAddress.startsWith("광주")) {
            if (shortenAddress.contains("광산")) {
                return Region.G01;
            } else if (shortenAddress.contains("남구")) {
                return Region.G02;
            } else if (shortenAddress.contains("동구")) {
                return Region.G03;
            } else if (shortenAddress.contains("북구")) {
                return Region.G04;
            } else if (shortenAddress.contains("서구")) {
                return Region.G05;
            }
        } else if (shortenAddress.startsWith("제주")) {
            if (shortenAddress.contains("서귀포")) {
                return Region.JJ01;
            } else if (shortenAddress.contains("제주")) {
                return Region.JJ02;
            }
        }

        return region;
    }
}
