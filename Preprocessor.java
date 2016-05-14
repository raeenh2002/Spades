import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.zip.GZIPInputStream;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.HashDocAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

public class Preprocessor extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	public static Preprocessor singleton = new Preprocessor(); 

	public ArrayList<ArrayList<ArrayList<Double>>> data, dataW, battery, batteryW;
	public ArrayList<ArrayList<Double>> prompts, annotations;
	@SuppressWarnings("serial")
	public static ArrayList<String> POSTURES = new ArrayList<String>() {{add("Lying down");add("Sitting");add("Standing mostly idle");
			add("Standing moving about");add("Walking");add("Jogging/Running");}};
	@SuppressWarnings("serial")
	public static ArrayList<String> PROMPTS = new ArrayList<String>() {{add("Reading/studying");add("Using computer/tablet");
			add("Talking on phone/computer");add("Texting");add("Taking a class");add("In a meeting");add("Doing physical labor");
			add("Talking with someone in person");add("Waiting for something");add("Cooking a meal");add("Preparing a snack");
			add("Eating a meal");add("Eating a snack");add("Enjoying a beverage");add("Cleaning up from eating/cooking");add("Dancing");
			add("Playing basketball, tennis, or soccer");add("Running/Jogging");add("Exercising/aerobics");add("Walking for fun/exercise");
			add("Walking a pet");add("Doing exercise class/Zumba");add("Stretching/Yoga");add("Bicycling indoors");add("Bicycling outdoors");
			add("Swimming");add("Weight lifting");add("Circuit training");add("Reading");add("Watching TV/movies");add("Using computer/tablet");
			add("Talking on the phone/computer");add("Playing video games");add("Sleeping/napping");add("Hanging out with someone");add("Walking");
			add("Riding in car/taxi");add("Driving in car");add("Riding a bus");add("Riding the Metro/train");add("Biking");add("Hanging out alone");
			add("Hanging out with someone");add("Cleaning up");add("Doing laundry");add("Doing other chores");add("Showering/bathing");add("Cooking");
			add("Getting ready for something");add("Doing yardwork");add("Getting dressed");add("Playing with child/pet (childcare)");add("Sleeping");
			add("Grocery shopping");add("Other shopping");add("Waiting for something/someone");add("Showering/bathing");add("Doing something else");}};
	@SuppressWarnings("serial")
	public static ArrayList<String> ANNOTATIONS = new ArrayList<String>() {{add("Attending church"); add("Baseball"); add("Basketball");
			add("Bathing (tub)");add("Bicycling"); add("Charging phone");add("Cleaning up"); add("Cooking/Baking"); add("Dance class");
			add("Doing chores"); add("Doing dishes");add("Doing laundry"); add("Driving car");add("Doing something else (sitting)");
			add("Doing something else (standing)");add("Doing something else (walking)");add("Doing something else");
			add("Eating/Drinking"); add("Eating/Drinking (sitting)");add("Eating/Drinking (standing)"); add("Eating/Drinking (walking)");
			add("Fitness class"); add("Fixing something");add("Football"); add("Getting dressed");add("Getting ready for something");
			add("Going somewhere"); add("Going somewhere (biking)");add("Going somewhere (car/bus/train)"); add("Going somewhere (skateboarding)");
			add("Going somewhere (walking)"); add("Going somewhere (running)");add("Hanging out (sitting)"); add("Hanging out (walking)");
			add("Hanging out (standing)");add("Hanging out"); add("Having conversation");add("Having conversation (sitting)");
			add("Having conversation (standing)");add("Having conversation (walking)");add("Hanging with friends"); add("I don't remember");
			add("In school"); add("In meeting");add("Jogging"); add("Karate class");add("Labeling my day"); add("Listen music");
			add("Listen music (sitting)"); add("Listen music (lying)");add("Lying down"); add("Mix of things");add("Playing catch");
			add("Playing instrument");add("Playing video games (standing)"); add("Playing video games (sitting)");add("Playing video games");
			add("Playing with child(ren)"); add("Child(ren) care");add("Playing with pet"); add("Putting around");add("Reading/Doing homework/Writing");
			add("Riding in a car"); add("Running");add("Shopping food"); add("Shopping other");add("Showering"); add("Showering/Bathing");
			add("Sitting"); add("Sitting in class");add("Skateboarding"); add("Skiing/Snowboarding");add("Sleeping"); add("Soccer");
			add("Sports/Exercising"); add("Standing");add("Swimming"); add("Taking bus");add("Taking stairs"); add("Taking train");
			add("Talking(phone/computer)"); add("Talking(phone/computer) (sitting)");add("Talking(phone/computer) (standing)");
			add("Talking(phone/computer) (walking)");add("Teaching"); add("Tennis/Racquetball");add("Texting"); add("Texting (sitting)");
			add("Texting (standing)"); add("Texting (walking)");add("Using computer/tablet"); add("Using computer/tablet (sitting)");
			add("Using computer/tablet (standing)"); add("Using other technology");add("Using phone for anything");
			add("Using phone for anything (walking)");add("Using phone for anything (sitting)");add("Using phone for anything (standing)");
			add("Using phone for anything (lying)");add("Using tools");add("Waiting");add("Waiting (sitting)");add("Waiting (standing)");
			add("Walking");add("Walking pet");add("Watching shows/movies");add("Watching TV");add("Weightlifting/Strength Training");
			add("Working/Job (sitting)");add("Working/Job (standing/walking)");}};
															
	Double number, sumMagnitude, numberW, sumMagnitudeW, numberB, sumMagnitudeB, numberBW, sumMagnitudeBW, CHUNK = 0.0084 /* 30 seconds*/;
	String YEAR;
	boolean dataLabelsFlag = true;
	boolean promptsFlag = true;

	@SuppressWarnings("deprecation")
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		
		String label = request.getParameter("label");
		String prompt = request.getParameter("prompt");
		
		if(label != null && (label.equals("N") || label.equals("n")))
			singleton.dataLabelsFlag = false;
		else if(label != null && (label.equals("Y") || label.equals("y")))
			singleton.dataLabelsFlag = true;
		
		if(prompt != null && (prompt.equals("N") || prompt.equals("n")))
			singleton.promptsFlag = false;
		else if(prompt != null && (prompt.equals("Y") || prompt.equals("y")))
			singleton.promptsFlag = true;

		
		PrintWriter out = response.getWriter();
		
		String html = "";

		html += "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">";
		html += "<html>";

		html += "<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.12.0/jquery.min.js\"></script>";
		html += "<script src=\"https://code.highcharts.com/stock/highstock.js\"></script>";
		html += "<script src=\"https://code.highcharts.com/modules/exporting.js\"></script>";
		html += "<script src=\"http://code.highcharts.com/modules/offline-exporting.js\"></script>";
		html += "<script src=\"https://code.highcharts.com/modules/boost.js\"></script>";
//		html += "<script src=\"https://github.com/niklasvh/html2canvas/releases/download/0.4.1/html2canvas.js\"></script>";
//		html += "<script src=\"https://cdn.rawgit.com/mrcoles/full-page-screen-capture-chrome-extension/master/page.js\"></script>";
//		html += "<script src=\"https://cdn.rawgit.com/mrcoles/full-page-screen-capture-chrome-extension/master/popup.js\"></script>";
//		html += "<script src=\"https://rawgit.com/sameerkatti/Shirter/master/scripts/canvas2image.js\"></script>";
//		html += "<script src=\"https://rawgit.com/hongru/canvas2image/master/canvas2image.js\"></script>";

		html += "<head>";
		html += "<title>Spades</title>";
//		html += "<style>"
//				+ "@media print {"
//				+ "	   		@page {"
//				+ "	   			size: 210mm 297mm;"
//				+ "	   			margin: 25mm;"
//				+ "	   			margin-right: 45mm;"
//				+ "	   		}"
//				+ "	   }</style>";
		html += "</head>\n";
		html += "<body>\n";

		html += "<div id=\"container\" style=\"width: 3300px; height: 4500px; margin: 0 auto\"></div>\n";
		
		html += "<script type=\"text/javascript\">\n";

		html += "$(function () {"
				+ "	   Highcharts.setOptions({"
				+ "	   		global: {"
	            + "	   			useUTC: false"
	            + "	   		}"
	            + "	   });"
				+ "    $('#container').highcharts('StockChart', {"
				+ "        chart: {"
				+ "            type: 'line',"
		        + "            margin: [20, 10, 50, 60],"
		        + "        },"
		        + "		   navigator : {"
                + "				enabled : false"
            	+ "		   },"
                + "		   exporting: {"
//                + "		   		sourceWidth: 2880,"
//                + "				sourceHeight: 4450,"
                + "		   		sourceWidth: 3300,"
                + "				sourceHeight: 5100,"
                + "				scale: 1,"
//                + "				chartOptions: {"
//                + "				}"
                + "		   },"
                + "		   plotOptions: {"
                + "		   		series: {"
                + "		   			connectNulls: false,"
                + "		   			enableMouseTracking: false,"
                + "		   			dataLabels: {"
                + "		   				enabled: " + ((singleton.dataLabelsFlag == true)? "true" : "false") + ","
                + "						crop: false, overflow: 'none', x: 7, y: 7,"
                + "		   				formatter: function() {"
                + "		   					if(this.series.options.id == 1 && this.y == 40) {"
                + "		   						return \"" + ANNOTATIONS.get(0) + "\";"
                + "		   					}\n";
				
				for(int i = 0; i < ANNOTATIONS.size(); i++) {
					
					html += "			else if(this.series.options.id == 1 && this.y == " + (double)(40.0 + i/2.0) + ") {"
		                + "		   				return \"" + ANNOTATIONS.get(i) + "\";"
		                + "		   			}\n";
				}
                
                html += "		   	},"
                + "		   			},"
                + "		   		},"
                + "				area: {"
                + "					lineColor: 'rgba(255,255,255,0)',"
                + "					lineWidth: 0,"
                + "				},"
                + "		   },"
		        + "		   scrollbar : {"
                + "				enabled : false"
            	+ "		   },"
		        + "		   rangeSelector : {"
                + "				enabled : false"
            	+ "		   },"
				+ "	       tooltip: {"
                + "	   	    	enabled: false,"
		        + "        },"
				+ "        title: {"
				+ "           text: '',"
				+ "            x: -20"
				+ "        },"
				+ "        subtitle: {"
				+ "            text: '',"
				+ "            x: -20"
				+ "        },"
				+ "        xAxis: [\n";
				
				for(int i = 0; i < singleton.data.size(); i++)
				{
					html += "{"
						+ "			   lineColor: 'transparent',"
						+ "			   tickLength: 0,"
						+ "            showLastLabel: true,"
						+ "            tickInterval: 3600 * 1000,"
						+ "			   ordinal: false,"
						+ "			   min: " + singleton.data.get(i).get(0).get(0).longValue() + ","
						+ "			   max: " + singleton.data.get(i).get(singleton.data.get(i).size() - 1).get(0).longValue() + ","
						+ "			   labels : {"
						+ "					enabled: " + ((i == 0)? "true" : "false") + ","
						+ "		   	   		format: '{value:%H}',"
						+ "					y: 30,"
						+ "					x: 0,"
						+ "					rotation: 0,"
						+ "		   	   },"
						+ "			   plotLines: [\n";
					
					if(singleton.promptsFlag)
						for(int j = 0; j < singleton.prompts.size(); j++)
						{
							if(new Date(singleton.prompts.get(j).get(0).longValue()).getDate() ==  new Date(singleton.data.get(i).get(0).get(0).longValue()).getDate())
								html += "{"
								+ "			   	value: " + singleton.prompts.get(j).get(0) + ","
			                    + "			   	color: '" + ((singleton.prompts.get(j).get(1) == 0.0)? "red" : "green") + "',"
			                    + "				dashStyle: 'shortdash'," //Solid
			                    + "		   		width: 2,"
			                    + "				zIndex: 100,"
			                    + "				label: {"
			                    + "					useHTML: true,"
			                    + "					text: '" + ((singleton.prompts.get(j).get(1) == 0.0 || singleton.prompts.get(j).get(3) == -1.0)? "" :
																	POSTURES.get(singleton.prompts.get(j).get(3).intValue())) + "<br>"
			                    								+ ((singleton.prompts.get(j).get(1) == 0.0 || singleton.prompts.get(j).get(2) == -1.0)? "" :
			                    									PROMPTS.get(singleton.prompts.get(j).get(2).intValue())) + "',"
			                    + "					rotation: 0,"
			                    + "					style: {"
			                    + "						color: 'green',"
			                    + "						verticalAlign: 'top',"
			                    + "						fontSize: 8,"
//			                    + "						fontWeight: 'bold',"
//			                    + "						y: -1000,"
			                    + "					},"
			                    + "				},"
			                    + "		 },\n";
						}

					html += "		],"
						+ "        },\n";
				}
				
				html += "],"
						+ "  yAxis: [\n";
		
				for(int i = 0; i < singleton.data.size(); i++)
				{
					Date date = new Date(singleton.data.get(i).get(0).get(0).longValue());
					
					html += "{"
					+ "			   opposite: false,"
					+ "			   lineColor: 'transparent',"
					+ "			   gridLineColor: 'transparent',"
					+ "			   title: {"
					+ "			   		text: '" + String.valueOf(date.getMonth() + 1) + "/" + 
													String.valueOf(date.getDate()) + "/" +
													String.valueOf(date.getYear() + 1900) + "',"
					+ "					x: -10,"
					+ "					y: 30,"
					+ "					rotation: -45,"
					+ "			   },"
					+ "            showLastLabel: true,"
					+ "			   getExtremesFromAll: true,"
					+ "			   labels : {"
					+ "					enabled: false,"
					+ "					align: 'right',"
					+ "					x: -5"
					+ "		   	   },"
					+ "			   offset: 5,"
					+ "            height: '" + ((double)Math.round((double)100/singleton.data.size() * 100)) / 100 + "%',"
					+ "            top: '" + ((double)Math.round((double)100 * i/singleton.data.size() * 100)) / 100 + "%',"
					+ "            tickPositions: [0.97, 1, 1.1, 1.15],"
					+ "			   startOnTick: true,"
					+ "            min: 0.97,"
					+ "            max: 1.15,"
					+ "            xAxis: " + i + ","
					+ "        }, {"
					+ "			   opposite: true,"
					+ "			   lineColor: 'transparent',"
					+ "			   gridLineColor: 'transparent',"
					+ "			   title: {"
					+ "			   		text: ''"
					+ "			   },"
					+ "            showLastLabel: true,"
					+ "			   getExtremesFromAll: true,"
					+ "			   labels : {"
					+ "					enabled: false,"
					+ "					align: 'left',"
					+ "					x: 0"
					+ "		   	   },"
					+ "			   offset: 5,"
					+ "            height: '" + ((double)Math.round((double)100/singleton.data.size() * 100)) / 100 + "%',"
					+ "            top: '" + ((double)Math.round((double)100 * i/singleton.data.size() * 100)) / 100 + "%',"
					+ "            tickPositions: [0, 1, 2, 3, 4, 5],"
					+ "			   startOnTick: true,"
					+ "            min: 0,"
					+ "            max: 4,"
					+ "            xAxis: " + i + ","
					+ "        }, {"
					+ "			   opposite: true,"
					+ "			   lineColor: 'transparent',"
					+ "			   gridLineColor: 'transparent',"
					+ "			   title: {"
					+ "			   		text: ''"
					+ "			   },"
					+ "            showLastLabel: true,"
					+ "			   getExtremesFromAll: true,"
					+ "			   labels : {"
					+ "					enabled: false,"
					+ "					align: 'left',"
					+ "					x: 0"
					+ "		   	   },"
					+ "			   offset: 5,"
					+ "            height: '" + ((double)Math.round((double)100/singleton.data.size() * 100)) / 100 + "%',"
					+ "            top: '" + ((double)Math.round((double)100 * i/singleton.data.size() * 100)) / 100 + "%',"
					+ "            tickPositions: [0, 50, 100],"
					+ "			   startOnTick: true,"
					+ "            min: 0,"
					+ "            max: 100,"
					+ "            xAxis: " + i + ","
					+ "        },\n";
				}
		
				html += "],"
				+ "		   credits: {"
				+ "			   enabled: false"
				+ "		   },"
				+ "        series: [\n";
				
				int count = 0;
				
				for(ArrayList<ArrayList<Double>> d : singleton.data)
				{
					html += 	"{"
						+ "			   data: " + d + ","
						+ "			   threshold : 1.05,"
						+ "			   color: 'black',"
				        + "			   negativeColor: 'black',"
						+ "            connectNulls: false,"
						+ "			   lineWidth: 4,"
						+ "            nullColor: '#EFEFEF',"
						+ "            colsize: 24 * 36e5,"
						+ "			   yAxis: " + count * 3 + ","
						+ "			   xAxis: " + count + ","
						+ "			   states: {"
						+ "		       		hover: {"
		                + "		   		    	enabled: false"
		                + "	           		}"
		                + "			   },"
						+ "            tooltip: {"
				        + "            		headerFormat: 'Magnitude<br/>',"
				        + "            		pointFormat: '{point.x:%e %b, %Y %H:%M:%S} : <b>{point.y:.2f}</b> '"
				        + "            },"
						+ "        },\n";
					
					count++;
				}
				
				count = 0;
				
				for(ArrayList<ArrayList<Double>> d : singleton.dataW)
				{
					html += 	"{"
						+ "			   data: " + d + ","
						+ "			   threshold : 1.5,"
						+ "			   color: 'rgba(255,30,0,0.7)',"
				        + "			   negativeColor: 'rgba(255,30,0,0.7)',"
						+ "            borderWidth: 0,"
						+ "            connectNulls: false,"
						+ "			   lineWidth: 4,"
						+ "            nullColor: '#EFEFEF',"
						+ "            colsize: 24 * 36e5,"
						+ "			   yAxis: " + (count * 3 + 1) + ","
						+ "			   xAxis: " + count + ","
						+ "			   states: {"
						+ "		       		hover: {"
		                + "		   		    	enabled: false"
		                + "	           		}"
		                + "			   },"
						+ "       	   tooltip: {"
				        + "            		headerFormat: 'Magnitude<br/>',"
				        + "            		pointFormat: '{point.x:%e %b, %Y %H:%M:%S} : <b>{point.y:.2f}</b>'"
				        + "            },"
						+ "        },\n";
					
					count++;
				}
				
				count = 0;
				
				for(ArrayList<ArrayList<Double>> b : singleton.battery)
				{
					html += 	"{"
						+ "			   data: " + b + ","
						+ "			   type : 'area',"
						+ "			   threshold : 1,"
						+ "			   color: 'rgba(0,0,255,0.3)',"
				        + "			   negativeColor: 'rgba(255,0,0,0.2)',"
						+ "            borderWidth: 0,"
						+ "            connectNulls: false,"
//						+ "			   lineWidth: 3,"
						+ "            nullColor: '#EFEFEF',"
						+ "            colsize: 24 * 36e5,"
						+ "			   yAxis: " + (count * 3 + 2) + ","
						+ "			   xAxis: " + count + ","
						+ "			   states: {"
						+ "		       		hover: {"
		                + "		   		    	enabled: false"
		                + "	           		}"
		                + "			   },"
						+ "       	   tooltip: {"
				        + "            		headerFormat: 'Magnitude<br/>',"
				        + "            		pointFormat: '{point.x:%e %b, %Y %H:%M:%S} : <b>{point.y:.2f}</b>'"
				        + "            },"
						+ "        },\n";
					
					count++;
				}
				
				count = 0;
				
				for(ArrayList<ArrayList<Double>> b : singleton.batteryW)
				{
					html += 	"{"
						+ "			   data: " + b + ","
						+ "			   type : 'area',"
						+ "			   threshold : 1,"
						+ "			   color: 'rgba(230,230,0,0.3)',"
				        + "			   negativeColor: 'rgba(255,0,0,0.2)',"
						+ "            borderWidth: 0,"
						+ "            connectNulls: false,"
//						+ "			   lineWidth: 3,"
						+ "            nullColor: '#EFEFEF',"
						+ "            colsize: 24 * 36e5,"
						+ "			   yAxis: " + (count * 3 + 2) + ","
						+ "			   xAxis: " + count + ","
						+ "			   states: {"
						+ "		       		hover: {"
		                + "		   		    	enabled: false"
		                + "	           		}"
		                + "			   },"
						+ "       	   tooltip: {"
				        + "            		headerFormat: 'Magnitude<br/>',"
				        + "            		pointFormat: '{point.x:%e %b, %Y %H:%M:%S} : <b>{point.y:.2f}</b>'"
				        + "            },"
						+ "        },\n";
					
					count++;
				}
				
				if(singleton.dataLabelsFlag)
					for(int i = 0; i < singleton.data.size(); i++)
					{	
						for(int j = 0; j < singleton.annotations.size(); j++)
						{
							if(singleton.data.get(i).size() > 0 &&
									new Date(singleton.annotations.get(j).get(0).longValue()).getDate() ==  new Date(singleton.data.get(i).get(0).get(0).longValue()).getDate() &&
									new Date(singleton.annotations.get(j).get(0).longValue()).getMonth() ==  new Date(singleton.data.get(i).get(0).get(0).longValue()).getMonth())
							{
								html += 	"{"
										+ "			   data: [{x:" + singleton.annotations.get(j).get(0) + ", y:" + singleton.annotations.get(j).get(2) + "," +
	//													"dataLabels: {enabled: true, allowOverlap: true, " +
	//													"align: 'left', verticalAlign: 'top', x: 0, y: -18, zIndex: 1000, " +
	//													"crop: false, overflow: 'none', inside: true, padding: 0, maxPadding: 0, " +
	//													"formatter: function() { return '" +
	//													ANNOTATIONS.get((int) ((singleton.annotations.get(j).get(2).longValue() - 50) / 20)) + "';}}" +
														"}, [" + singleton.annotations.get(j).get(1) + "," + singleton.annotations.get(j).get(2) + "]],"
										+ "			   id: 1,"
										+ "			   type : 'line',"
										+ "			   color: 'rgba(" + (singleton.annotations.get(j).get(2)).longValue() * 2 + "," +
														(int)(200 - singleton.annotations.get(j).get(2) * 2) + "," +
														(int)(255 - singleton.annotations.get(j).get(2)) + ", 0.8)',"
										+ "            borderWidth: 0,"
										+ "            connectNulls: false,"
										+ "			   lineWidth: 4,"
										+ "            nullColor: '#EFEFEF',"
										+ "            colsize: 24 * 36e5,"
										+ "			   yAxis: " + (i * 3 + 2) + ","
										+ "			   xAxis: " + i + ","
										+ "			   states: {"
										+ "		       		hover: {"
						                + "		   		    	enabled: false"
						                + "	           		}"
						                + "			   },"
										+ "       	   tooltip: {"
								        + "            		headerFormat: 'Magnitude<br/>',"
								        + "            		pointFormat: '{point.x:%e %b, %Y %H:%M:%S} : <b>{point.y:.2f}</b>'"
								        + "            },"
										+ "        },\n";
							}
						}
					}
				
				html += "],"
						+ "    });"
						+ "}); "
				

				+ "$(\"path\").removeAttr(\"visibility\");\n";
				
//				+ "Highcharts.Renderer.prototype.symbols.hline = function(x, y, width, height) {"
//				+ 		"return ['M',x ,y + width / 2,'L',x+height,y + width / 2];"
//				+ "};\n";
				
				html += "var screenshot = '';";
				
				html += "function max(nums) {"
						+ "return Math.max.apply(Math, nums.filter(function(x) { return x; }));"
					+ "}";
				
				html += "function print() {"
						+ "	var body = document.body,"
						+ "	widths = ["
				        + "	    document.documentElement.clientWidth,"
				        + "	    document.body.scrollWidth,"
				        + "	    document.documentElement.scrollWidth,"
				        + "	    document.body.offsetWidth,"
				        + "	    document.documentElement.offsetWidth"
				        + "	],"
				        + "	heights = ["
				        + "	    document.documentElement.clientHeight,"
				        + "	    document.body.scrollHeight,"
				        + "	    document.documentElement.scrollHeight,"
				        + "	    document.body.offsetHeight,"
				        + "	    document.documentElement.offsetHeight"
				        + "	],"
				        + "	fullWidth = max(widths),"
				        + "	fullHeight = max(heights),"
				        + "	windowWidth = window.innerWidth,"
				        + "	windowHeight = window.innerHeight,"
				        + "	originalX = window.scrollX,"
				        + "	originalY = window.scrollY,"
				        + "	originalOverflowStyle = document.documentElement.style.overflow,"
				        + "	arrangements = [],"				        
				        + "	scrollPad = 200,"
				        + "	yDelta = windowHeight - (windowHeight > scrollPad ? scrollPad : 0),"
				        + "	xDelta = windowWidth,"
				        + "	yPos = fullHeight - windowHeight,"
				        + "	xPos,"
				        + "	numArrangements;"
 
				    + "	if (fullWidth <= xDelta + 1) {"
				        + "	fullWidth = xDelta;"
		        	+ "	}"

				    + "	document.documentElement.style.overflow = 'hidden';"

				    + "	while (yPos > -yDelta) {"
				        + "	xPos = 0;"
				        + "	while (xPos < fullWidth) {"
				        + "	    arrangements.push([xPos, yPos]);"
				        + "	    xPos += xDelta;"
				        + "	}"
				        + "	yPos -= yDelta;"
			        + "	}"

				    + "	numArrangements = arrangements.length;"

				    + "	function cleanUp() {"
				    + "	    document.documentElement.style.overflow = originalOverflowStyle;"
				    + "	    window.scrollTo(originalX, originalY);"
				    + "	}"

				    + "	(function processArrangements() {"
				        + "	if (!arrangements.length) {"
				            + "	cleanUp();"
//				            + "	if (callback) {"
//				            + "	    callback();"
//				            + "	}"
				            + "	return;"
			            + "	}"

				        + "	var next = arrangements.shift(),"
				            + "	x = next[0], y = next[1];"

				        + "	window.scrollTo(x, y);"

				        + "	var data = {"
				            + "	msg: 'capturePage',"
				            + "	x: window.scrollX,"
				            + "	y: window.scrollY,"
				            + "	complete: (numArrangements-arrangements.length)/numArrangements,"
				            + "	totalWidth: fullWidth,"
				            + "	totalHeight: fullHeight,"
				            + "	devicePixelRatio: window.devicePixelRatio"
			            + "	};"
				        
				        + "	window.setTimeout(function() {"				            
				            + "	var cleanUpTimeout = window.setTimeout(cleanUp, 1250);"
				            + "	if(capturePage(data))"
				            	+ "	processArrangements();"
			            + "	}, 150);"
		            + "	})();"
    				+"}"
		            
    				+ "	function capturePage(data) {"
	    				+ "	var canvas;"
	
//	    				+ "	$('bar').style.width = parseInt(data.complete * 100, 10) + '%';"
	
	    				+ "	var scale = data.devicePixelRatio && data.devicePixelRatio !== 1 ?"
	    					+ "	1 / data.devicePixelRatio : 1;"
	    
					    + "	if (scale !== 1) {"
					        + "	data.x = data.x / scale;"
					        + "	data.y = data.y / scale;"
					        + "	data.totalWidth = data.totalWidth / scale;"
					        + "	data.totalHeight = data.totalHeight / scale;"
						+ "	}"
	
	
					    + "	if (!screenshot.canvas) {"
					        + "	canvas = document.createElement('canvas');"
					        + "	canvas.width = data.totalWidth;"
					        + "	canvas.height = data.totalHeight;"
					        + "	screenshot.canvas = canvas;"
					        + "	screenshot.ctx = canvas.getContext('2d');debugger;"
					        
			                + "	var image = new Image();"
			                + "	image.onload = function() {"                    
			                    + "	screenshot.ctx.drawImage(image, data.x, data.y);"
			                    + "	return true;"
			                + "	};"
					    + "	}"
				    + "	}"
    				
    				+"window.onload = print;";

		html += "</script>\n";

		html += "</body>\n";
		html += "</html>\n";
		
		out.println(html);
		
		out.close();
		
		out = new PrintWriter("index.jsp");
		
		out.println(html);
		
		out.close();
		
		PrintRequestAttributeSet pras = 
        		new HashPrintRequestAttributeSet();
        DocFlavor flavor = DocFlavor.INPUT_STREAM.PNG;
        PrintService defaultService = 
        		PrintServiceLookup.lookupDefaultPrintService();

        DocPrintJob job = defaultService.createPrintJob();
        FileInputStream fis = new FileInputStream("index.jsp");
        DocAttributeSet das = new HashDocAttributeSet();
        Doc doc = new SimpleDoc(fis, flavor, das);
        
        try {
			job.print(doc, pras);
		} catch (PrintException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// request.getRequestDispatcher("index.jsp").forward(request, response);
	}

	public void init() throws ServletException {

		try {
			
//			JAXBContext jaxbContext = JAXBContext.newInstance(Activity.class);
//			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
//			Activity annotations = (Activity) jaxbUnmarshaller.unmarshal(new File("annotations.xml"));

			Runnable r1 = new Runnable1();
	        Thread t1 = new Thread(r1);
	        t1.start();
	        
	        Runnable r2 = new Runnable2();
	        Thread t2 = new Thread(r2);
	        t2.start();
	        
	        Runnable r3 = new Runnable3();
	        Thread t3 = new Thread(r3);
	        t3.start();
	        
	        Runnable r4 = new Runnable4();
	        Thread t4 = new Thread(r4);
	        t4.start();
			
	        Preprocessor.singleton.prompts = getPrompts();
	        Preprocessor.singleton.annotations = getAnnotations();
	        
	        t1.join();
	        t2.join();
	        t3.join();
	        t4.join();	     	       

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@SuppressWarnings({ "serial" })
	private ArrayList<ArrayList<Double>> getPrompts() throws IOException, ParseException {
		
		ArrayList<ArrayList<Double>> ret = new ArrayList<ArrayList<Double>>();
		
		File folder = new File("survey");
		if(!folder.exists()) {
			Preprocessor.singleton.promptsFlag = false;
			return new ArrayList<ArrayList<Double>>();
		}
		File[] listOfFiles = folder.listFiles();
		
		for (int i = 0; i < listOfFiles.length; i++) {
			
			InputStream csv = null, csvR = null;
			
			try {
				for(File alpha : listOfFiles[i].listFiles()) {
					
					if(alpha.getName().toLowerCase().contains("prompts.csv") && alpha.isFile()) {
						
						csv = new FileInputStream(alpha.getPath());
					}
					
					if(alpha.getName().toLowerCase().contains("prompt") && alpha.getName().toLowerCase().contains("responses") &&
							alpha.getName().toLowerCase().endsWith("csv") && alpha.isFile()) {
						
						csvR = new FileInputStream(alpha.getPath());
					}
				}
				
			} catch (Exception ignored) {
				continue;
			}
			
			if(csv == null || csvR == null)
				continue;
			
			Reader decoder = new InputStreamReader(csv, "UTF-8"), decoderR = new InputStreamReader(csvR, "UTF-8");
			BufferedReader buffered = new BufferedReader(decoder), bufferedR = new BufferedReader(decoderR);

			String text = buffered.readLine();
			text = buffered.readLine();
			
			String textR = bufferedR.readLine();
			textR = bufferedR.readLine();

			while (text != null && countOccurrences(text, ',') < 7) {
				
				DateFormat df = new SimpleDateFormat("y-M-d k:m:s", Locale.ENGLISH);

				Date date = df.parse(text.substring(1, text.indexOf(",") - 1));
				
				double completed = (text.substring(text.lastIndexOf(",") + 2).contains("Never"))? 0.0 : 1.0;
				
				double activity = -1.0;
				double posture = -1.0;
				
				int tenthCommaIndex = textR.indexOf(",",
												textR.indexOf(",",
														textR.indexOf(",",
																textR.indexOf(",",
																		textR.indexOf(",",
																				textR.indexOf(",",
																						textR.indexOf(",",
																								textR.indexOf(",")
									+ 1) + 1) + 1) + 1) + 1) + 1) + 1);
				
				for(int k = 0; k < POSTURES.size(); k++)
					if(textR.indexOf("\",", tenthCommaIndex + 1) > 0 && textR.substring(tenthCommaIndex + 1, textR.indexOf("\",", tenthCommaIndex + 1))
							.toLowerCase().contains(POSTURES.get(k).toLowerCase()))
					{
						posture = k;
						break;
					}
				
				for(int k = 0; k < PROMPTS.size(); k++)
					if(textR.substring(textR.indexOf(",", tenthCommaIndex + 1)).toLowerCase().contains(PROMPTS.get(k).toLowerCase()))
					{
						activity = k;
						break;
					}
				
				double act = activity;
				double pos = posture;
				
				ret.add(new ArrayList<Double>() {
					{
						add((double) date.getTime());
						add(completed);
						add(act);
						add(pos);
					}
				});
				
				text = buffered.readLine();
				textR = bufferedR.readLine();
				
			}
			
			buffered.close();
			bufferedR.close();
		}
		
		return ret;	
		
	}

	@SuppressWarnings({ "serial" })
	public ArrayList<ArrayList<Double>> getAnnotations() throws IOException, ParseException {
		////////////////////////////////////////////////////////////////////////////////////////////// Phone Battery

		ArrayList<ArrayList<Double>> ret = new ArrayList<ArrayList<Double>>();
		
		File folder = new File("data");
		File[] listOfYears = folder.listFiles();		

		for(int o = 0; o < listOfYears.length; o++) {
			
			File[] listOfMonths = listOfYears[o].listFiles();
			
			for(int k = 0; k < listOfMonths.length; k++) {
				for (int j = 1; j <= 31; j++) {
					for (int i = 0; i < 24; i++) {
						String DAY = ((Integer.toString(j).length() < 2) ? ("0" + Integer.toString(j)) : (Integer.toString(j)));
		
						String HOUR = ((Integer.toString(i).length() < 2) ? ("0" + Integer.toString(i))
								: (Integer.toString(i)));
		
						InputStream gz;
		
						try {
							File temp = new File(listOfMonths[k].getPath() + "/" + DAY + "/" + HOUR);
							File[] listOfTemps = temp.listFiles();
							File file = null;
							
							for(File alpha : listOfTemps) {
								if(alpha.getName().contains("annotation") && alpha.getName().toLowerCase().endsWith("csv.gz") && alpha.isFile()) {
									file = alpha;
									break;
								}
							}
							
							gz = new GZIPInputStream(new FileInputStream(file.getPath()));
						} catch (Exception ignored) {
							
							continue;
						}
		
						Reader decoder = new InputStreamReader(gz, "UTF-8");
						BufferedReader buffered = new BufferedReader(decoder);
		
						String text = buffered.readLine();
						text = buffered.readLine();
		
						while (text != null && countOccurrences(text, ',') < 8) {
		
							DateFormat df = new SimpleDateFormat("y-M-d k:m:s.S", Locale.ENGLISH);
		
							Date fromDate = df.parse(text.substring(text.indexOf(",") + 1, text.indexOf(",", text.indexOf(",") + 1)));
							Date toDate = df.parse(text.substring(text.indexOf(",", text.indexOf(",") + 1) + 1,
																	text.indexOf(",", text.indexOf(",", text.indexOf(",") + 1) + 1)));
							
							double height = 0.0;
							for(int index = 0; index < ANNOTATIONS.size(); index++) {
								if(text.toLowerCase().contains(ANNOTATIONS.get(index).toLowerCase())) {
									height = 40 + index / 2.0;
									break;
								}
							}
							
							double type = height;
							
							ret.add(new ArrayList<Double>() {
								{
									add((double) fromDate.getTime());
									add((double) toDate.getTime());
									add(type);
								}
							});
							
							text = buffered.readLine();
										
						}
		
						buffered.close();
					}
				}
			}
		}

		return ret;

	}

	@SuppressWarnings({ "serial", "deprecation" })
	public ArrayList<ArrayList<ArrayList<Double>>> getBattery() throws IOException, ParseException {
		////////////////////////////////////////////////////////////////////////////////////////////// Phone Battery

		ArrayList<ArrayList<ArrayList<Double>>> ret = new ArrayList<ArrayList<ArrayList<Double>>>();
		
		ArrayList<ArrayList<Double>> day = new ArrayList<ArrayList<Double>>();
		
		File folder = new File("data");
		
		File[] listOfYears = folder.listFiles();		

		for(int o = 0; o < listOfYears.length; o++) {
			
			File[] listOfMonths = listOfYears[o].listFiles();

			for(int k = 0; k < listOfMonths.length; k++) {
				for (int j = 1; j <= 31; j++) {
					for (int i = 0; i < 24; i++) {
						String DAY = ((Integer.toString(j).length() < 2) ? ("0" + Integer.toString(j)) : (Integer.toString(j)));
		
						String HOUR = ((Integer.toString(i).length() < 2) ? ("0" + Integer.toString(i))
								: (Integer.toString(i)));
						
						int MONTH = Integer.parseInt(listOfMonths[k].getName()) - 1;
						
						int YEAR = Integer.parseInt(listOfYears[o].getName());
		
						InputStream gz;
		
						try {
							File temp = new File(listOfMonths[k].getPath() + "/" + DAY + "/" + HOUR);
							File[] listOfTemps = temp.listFiles();
							File file = null;
							
							for(File alpha : listOfTemps) {
								if(alpha.getName().toLowerCase().contains("battery") &&
										alpha.getName().toLowerCase().contains("event") && 
										alpha.getName().toLowerCase().endsWith("csv.gz") && alpha.isFile()) {
									for(File beta : listOfTemps) {
										if(beta.getName().toLowerCase().contains("phone") &&
												beta.getName().toLowerCase().contains(alpha.getName().toLowerCase()
														.substring(alpha.getName().toLowerCase().indexOf(".") + 1,
																alpha.getName().toLowerCase().indexOf("-")))) {
											file = alpha;
											break;
										}
									}									
								}
							}
							
							gz = new GZIPInputStream(new FileInputStream(file.getPath()));
						} catch (Exception ignored) {
							
							int count = 0;
							for(File file : listOfMonths[k].listFiles()) {
								if(file.getName().contains(DAY)) {
									count++;
									break;
								}
							}
							
							if(count == 0)
								break;
							
							day.add(new ArrayList<Double>() {
								{
									add((double) new Date(YEAR - 1900, MONTH, Integer.parseInt(DAY), Integer.parseInt(HOUR), 0).getTime());
									add(null);
								}
							});
							day.add(new ArrayList<Double>() {
								{
									add((double) new Date(YEAR - 1900, MONTH, Integer.parseInt(DAY), Integer.parseInt(HOUR), 59).getTime());
									add(null);
								}
							});
							continue;
						}
		
						Reader decoder = new InputStreamReader(gz, "UTF-8");
						BufferedReader buffered = new BufferedReader(decoder);
		
						String text = buffered.readLine();
						text = buffered.readLine();
		
						while (text != null && countOccurrences(text, ',') < 5) {
		
							DateFormat df = new SimpleDateFormat("y-M-d k:m:s.S", Locale.ENGLISH);
							DateFormat tf = new SimpleDateFormat("k:m:s");
		
							Date aveDate = df.parse(text.substring(0, text.indexOf(",")));
		
							String avetime = tf.format(aveDate);
							final double avehour = (double) Integer.parseInt(avetime.substring(0, avetime.indexOf(":")))
									+ (double) Integer
											.parseInt(avetime.substring(avetime.indexOf(":") + 1, avetime.lastIndexOf(":")))
											/ 60
									+ (double) Integer.parseInt(avetime.substring(avetime.lastIndexOf(":") + 1)) / 3600;
							double hour = avehour;
		
							this.sumMagnitudeB = 0.0;
							this.numberB = 0.0;
		
							do {
							
								numberB++;
								Date datetime;
								
								try {
									
									datetime = df.parse(text.substring(0, text.indexOf(",")));
									
								} catch(ParseException e) {
									
									text = buffered.readLine();
									numberB--;
									continue;
								}
								
								if(aveDate.getDate() != datetime.getDate())
								{
									text = buffered.readLine();
									numberB--;
									continue;
								}
								
								String time = tf.format(datetime);
								hour = (double) Integer.parseInt(time.substring(0, time.indexOf(":")))
										+ (double) Integer
												.parseInt(time.substring(time.indexOf(":") + 1, time.lastIndexOf(":"))) / 60
										+ (double) Integer.parseInt(time.substring(time.lastIndexOf(":") + 1)) / 3600;
		
			
								Double magnitude = Double.parseDouble(text.substring(
														text.indexOf(",", text.indexOf(",", text.indexOf(",") + 1) + 1) + 1, text.lastIndexOf(",")));
								
								this.sumMagnitudeB += magnitude;
								text = buffered.readLine();
							
							} while (hour - avehour < 0 && text != null && countOccurrences(text, ',') < 5);
		
							day.add(new ArrayList<Double>() {
								{
									add((double) aveDate.getTime());
									add(Math.floor(singleton.sumMagnitudeB / numberB * 100) / 100);
								}
							});
										
						}
		
						buffered.close();
					}
								
					if(!day.isEmpty())
						ret.add(day);
					
					day = new ArrayList<ArrayList<Double>>();
				}
			}
		}

		return ret;

	}

	@SuppressWarnings({ "serial", "deprecation" })
	public ArrayList<ArrayList<ArrayList<Double>>> getBatteryW() throws IOException, ParseException {
		////////////////////////////////////////////////////////////////////////////////////////////// Phone Battery

		ArrayList<ArrayList<ArrayList<Double>>> ret = new ArrayList<ArrayList<ArrayList<Double>>>();
		
		ArrayList<ArrayList<Double>> day = new ArrayList<ArrayList<Double>>();
		
		File folder = new File("data");

		File[] listOfYears = folder.listFiles();		

		for(int o = 0; o < listOfYears.length; o++) {
			
			File[] listOfMonths = listOfYears[o].listFiles();

			for(int k = 0; k < listOfMonths.length; k++) {
				for (int j = 1; j <= 31; j++) {
					for (int i = 0; i < 24; i++) {
						String DAY = ((Integer.toString(j).length() < 2) ? ("0" + Integer.toString(j)) : (Integer.toString(j)));
		
						String HOUR = ((Integer.toString(i).length() < 2) ? ("0" + Integer.toString(i))
								: (Integer.toString(i)));
		
						int MONTH = Integer.parseInt(listOfMonths[k].getName()) - 1;
						
						int YEAR = Integer.parseInt(listOfYears[o].getName());
						
						InputStream gz;
		
						try {
							File temp = new File(listOfMonths[k].getPath() + "/" + DAY + "/" + HOUR);
							File[] listOfTemps = temp.listFiles();
							File file = null;						
							
							for(File alpha : listOfTemps) {
								if(alpha.getName().toLowerCase().contains("battery") &&
										alpha.getName().toLowerCase().contains("event") &&
										alpha.getName().toLowerCase().endsWith("csv.gz") && alpha.isFile()) {
									for(File beta : listOfTemps) {
										if(beta.getName().toLowerCase().contains("watch") &&
												beta.getName().toLowerCase().contains(alpha.getName().toLowerCase()
														.substring(alpha.getName().toLowerCase().indexOf(".") + 1, 
																alpha.getName().toLowerCase().indexOf("-")))) {
											file = alpha;
											break;
										}
									}									
								}
							}					
							
							gz = new GZIPInputStream(new FileInputStream(file.getPath()));
						} catch (Exception ignored) {
							
							int count = 0;
							for(File file : listOfMonths[k].listFiles()) {
								if(file.getName().contains(DAY)) {
									count++;
									break;
								}
							}
							
							if(count == 0)
								break;
							
							day.add(new ArrayList<Double>() {
								{
									add((double) new Date(YEAR - 1900, MONTH, Integer.parseInt(DAY), Integer.parseInt(HOUR), 0).getTime());
									add(null);
								}
							});
							day.add(new ArrayList<Double>() {
								{
									add((double) new Date(YEAR - 1900, MONTH, Integer.parseInt(DAY), Integer.parseInt(HOUR), 59).getTime());
									add(null);
								}
							});
							continue;
						}
		
						Reader decoder = new InputStreamReader(gz, "UTF-8");
						BufferedReader buffered = new BufferedReader(decoder);
		
						String text = buffered.readLine();
						text = buffered.readLine();
		
						while (text != null && countOccurrences(text, ',') < 5) {
		
							DateFormat df = new SimpleDateFormat("y-M-d k:m:s.S", Locale.ENGLISH);
							DateFormat tf = new SimpleDateFormat("k:m:s");
		
							Date aveDate = df.parse(text.substring(0, text.indexOf(",")));
		
							String avetime = tf.format(aveDate);
							final double avehour = (double) Integer.parseInt(avetime.substring(0, avetime.indexOf(":")))
									+ (double) Integer
											.parseInt(avetime.substring(avetime.indexOf(":") + 1, avetime.lastIndexOf(":")))
											/ 60
									+ (double) Integer.parseInt(avetime.substring(avetime.lastIndexOf(":") + 1)) / 3600;
							double hour = avehour;
		
							this.sumMagnitudeBW = 0.0;
							this.numberBW = 0.0;
		
							do {
							
								numberBW++;
								Date datetime;
								
								try {
									
									datetime = df.parse(text.substring(0, text.indexOf(",")));
									
								} catch(ParseException e) {
									
									text = buffered.readLine();
									numberBW--;
									continue;
								}
								
								if(aveDate.getDate() != datetime.getDate())
								{
									text = buffered.readLine();
									numberBW--;
									continue;
								}
								
								String time = tf.format(datetime);
								hour = (double) Integer.parseInt(time.substring(0, time.indexOf(":")))
										+ (double) Integer
												.parseInt(time.substring(time.indexOf(":") + 1, time.lastIndexOf(":"))) / 60
										+ (double) Integer.parseInt(time.substring(time.lastIndexOf(":") + 1)) / 3600;
		
			
								Double magnitude = Double.parseDouble(text.substring(
														text.indexOf(",", text.indexOf(",", text.indexOf(",") + 1) + 1) + 1, text.lastIndexOf(",")));
								
								this.sumMagnitudeBW += magnitude;
								text = buffered.readLine();
							
							} while (hour - avehour < 0 && text != null && countOccurrences(text, ',') < 5);
		
							day.add(new ArrayList<Double>() {
								{
									add((double) aveDate.getTime());
									add(Math.floor(singleton.sumMagnitudeBW / numberBW * 100) / 100);
								}
							});
										
						}
		
						buffered.close();
					}
								
					if(!day.isEmpty())
						ret.add(day);
					
					day = new ArrayList<ArrayList<Double>>();
				}
			}
		}

		return ret;

	}

	@SuppressWarnings({ "serial", "deprecation" })
	public ArrayList<ArrayList<ArrayList<Double>>> getData() throws IOException, ParseException {
		////////////////////////////////////////////////////////////////////////////////////////////// Phone

		ArrayList<ArrayList<ArrayList<Double>>> ret = new ArrayList<ArrayList<ArrayList<Double>>>();
		
		ArrayList<ArrayList<Double>> day = new ArrayList<ArrayList<Double>>();
		
		File folder = new File("data");

		File[] listOfYears = folder.listFiles();		

		for(int o = 0; o < listOfYears.length; o++) {
			
			File[] listOfMonths = listOfYears[o].listFiles();

			for(int k = 0; k < listOfMonths.length; k++) {
				for (int j = 1; j <= 31; j++) {
					for (int i = 0; i < 24; i++) {
						String DAY = ((Integer.toString(j).length() < 2) ? ("0" + Integer.toString(j)) : (Integer.toString(j)));
		
						String HOUR = ((Integer.toString(i).length() < 2) ? ("0" + Integer.toString(i))
								: (Integer.toString(i)));
		
						int MONTH = Integer.parseInt(listOfMonths[k].getName()) - 1;
						
						int YEAR = Integer.parseInt(listOfYears[o].getName());
						
						InputStream gz;
		
						try {
							File temp = new File(listOfMonths[k].getPath() + "/" + DAY + "/" + HOUR);
							File[] listOfTemps = temp.listFiles();
							File file = null;
							
							for(File alpha : listOfTemps) {
								if(alpha.getName().toLowerCase().contains("phone") &&
										alpha.getName().toLowerCase().contains("calibrated") &&
										alpha.getName().toLowerCase().contains("sensor") &&
										alpha.getName().toLowerCase().endsWith("csv.gz") && alpha.isFile()) {
									file = alpha;
									break;
								}
							}
							
							gz = new GZIPInputStream(new FileInputStream(file.getPath()));
						} catch (Exception ignored) {
							
							int count = 0;
							for(File file : listOfMonths[k].listFiles()) {
								if(file.getName().contains(DAY)) {
									count++;
									break;
								}
							}
							
							if(count == 0)
								break;
							
							day.add(new ArrayList<Double>() {
								{
									add((double) new Date(YEAR - 1900, MONTH, Integer.parseInt(DAY), Integer.parseInt(HOUR), 0).getTime());
									add(null);
								}
							});
							day.add(new ArrayList<Double>() {
								{
									add((double) new Date(YEAR - 1900, MONTH, Integer.parseInt(DAY), Integer.parseInt(HOUR), 59).getTime());
									add(null);
								}
							});
							continue;
						}
		
						Reader decoder = new InputStreamReader(gz, "UTF-8");
						BufferedReader buffered = new BufferedReader(decoder);
		
						String text = buffered.readLine();
						text = buffered.readLine();
		
						while (text != null && countOccurrences(text, ',') < 4) {
		
							DateFormat df = new SimpleDateFormat("y-M-d k:m:s.S", Locale.ENGLISH);
							DateFormat tf = new SimpleDateFormat("k:m:s");
		
							Date aveDate = df.parse(text.substring(0, text.indexOf(",")));
		
							String avetime = tf.format(aveDate);
							final double avehour = (double) Integer.parseInt(avetime.substring(0, avetime.indexOf(":")))
									+ (double) Integer
											.parseInt(avetime.substring(avetime.indexOf(":") + 1, avetime.lastIndexOf(":")))
											/ 60
									+ (double) Integer.parseInt(avetime.substring(avetime.lastIndexOf(":") + 1)) / 3600;
							double hour = avehour;
		
							this.sumMagnitude = 0.0;
							this.number = 0.0;
		
							do {
							
								number++;
								Date datetime;
								
								try {
									
									datetime = df.parse(text.substring(0, text.indexOf(",")));
									
								} catch(ParseException e) {
									
									text = buffered.readLine();
									number--;
									continue;
								}
								
								if(aveDate.getDate() != datetime.getDate())
								{
									text = buffered.readLine();
									number--;
									continue;
								}
								
								String time = tf.format(datetime);
								hour = (double) Integer.parseInt(time.substring(0, time.indexOf(":")))
										+ (double) Integer
												.parseInt(time.substring(time.indexOf(":") + 1, time.lastIndexOf(":"))) / 60
										+ (double) Integer.parseInt(time.substring(time.lastIndexOf(":") + 1)) / 3600;
		
			
								Double magnitude = Math
										.sqrt(Math
												.pow(Double.parseDouble(text.substring(text.indexOf(",") + 1,
														text.indexOf(",", text.indexOf(",") + 1))), 2)
												+ Math.pow(Double.parseDouble(text.substring(
														text.indexOf(",", text.indexOf(",") + 1) + 1, text.lastIndexOf(","))), 2)
												+ Math.pow(Double.parseDouble(text.substring(text.lastIndexOf(",") + 1)), 2));
								
								this.sumMagnitude += magnitude;
								text = buffered.readLine();
							
							} while (hour - avehour < CHUNK && text != null && countOccurrences(text, ',') < 4);
							
							day.add(new ArrayList<Double>() {
								{
									add((double) aveDate.getTime());
									add(Math.floor(singleton.sumMagnitude / number * 100) / 100);
								}
							});
										
						}
		
						buffered.close();
					}
								
					if(!day.isEmpty())
						ret.add(day);
					
					day = new ArrayList<ArrayList<Double>>();
				}
			}
		}

		return ret;

	}

	@SuppressWarnings({ "serial", "deprecation" })
	public ArrayList<ArrayList<ArrayList<Double>>> getDataW() throws IOException, ParseException {

		/////////////////////////////////////////////////////////////////////////////// WATCH

		ArrayList<ArrayList<ArrayList<Double>>> ret = new ArrayList<ArrayList<ArrayList<Double>>>();
		
		ArrayList<ArrayList<Double>> day = new ArrayList<ArrayList<Double>>();
		
		File folder = new File("data");

		File[] listOfYears = folder.listFiles();		

		for(int o = 0; o < listOfYears.length; o++) {
			
			File[] listOfMonths = listOfYears[o].listFiles();

			for(int k = 0; k < listOfMonths.length; k++) {
				for (int j = 1; j <= 31; j++) {
					for (int i = 0; i < 24; i++) {
						String DAY = ((Integer.toString(j).length() < 2) ? ("0" + Integer.toString(j)) : (Integer.toString(j)));
		
						String HOUR = ((Integer.toString(i).length() < 2) ? ("0" + Integer.toString(i))
								: (Integer.toString(i)));
		
						int MONTH = Integer.parseInt(listOfMonths[k].getName()) - 1;
						
						int YEAR = Integer.parseInt(listOfYears[o].getName());
						
						InputStream gz;
		
						try {
							File temp = new File(listOfMonths[k].getPath() + "/" + DAY + "/" + HOUR);
							File[] listOfTemps = temp.listFiles();
							File file = null;
							
							for(File alpha : listOfTemps) {
								if(alpha.getName().toLowerCase().contains("watch") &&
										alpha.getName().toLowerCase().contains("calibrated") &&
										alpha.getName().toLowerCase().contains("sensor") &&
										alpha.getName().toLowerCase().endsWith("csv.gz") && alpha.isFile()) {
									file = alpha;
									break;
								}
							}
							
							gz = new GZIPInputStream(new FileInputStream(file.getPath()));
						} catch (Exception ignored) {
							
							int count = 0;
							for(File file : listOfMonths[k].listFiles()) {
								if(file.getName().contains(DAY)) {
									count++;
									break;
								}
							}
							
							if(count == 0)
								break;
							
							day.add(new ArrayList<Double>() {
								{
									add((double) new Date(YEAR - 1900, MONTH, Integer.parseInt(DAY), Integer.parseInt(HOUR), 0).getTime());
									add(null);
								}
							});
							day.add(new ArrayList<Double>() {
								{
									add((double) new Date(YEAR - 1900, MONTH, Integer.parseInt(DAY), Integer.parseInt(HOUR), 59).getTime());
									add(null);
								}
							});
							continue;
						}
		
						Reader decoder = new InputStreamReader(gz, "UTF-8");
						BufferedReader buffered = new BufferedReader(decoder);
		
						String text = buffered.readLine();
						text = buffered.readLine();
		
						while (text != null && countOccurrences(text, ',') < 4) {
		
							DateFormat df = new SimpleDateFormat("y-M-d k:m:s.S", Locale.ENGLISH);
							DateFormat tf = new SimpleDateFormat("k:m:s");
		
							Date aveDate = df.parse(text.substring(0, text.indexOf(",")));
		
							String avetime = tf.format(aveDate);
							final double avehour = (double) Integer.parseInt(avetime.substring(0, avetime.indexOf(":")))
									+ (double) Integer
											.parseInt(avetime.substring(avetime.indexOf(":") + 1, avetime.lastIndexOf(":")))
											/ 60
									+ (double) Integer.parseInt(avetime.substring(avetime.lastIndexOf(":") + 1)) / 3600;
							double hour = avehour;
		
							this.sumMagnitudeW = 0.0;
							this.numberW = 0.0;
		
							do {
							
								numberW++;
								Date datetime;
								
								try {
									
									datetime = df.parse(text.substring(0, text.indexOf(",")));
									
								} catch(ParseException e) {
									
									text = buffered.readLine();
									continue;
								}
								
								if(aveDate.getDate() != datetime.getDate())
								{
									text = buffered.readLine();
									numberW--;
									continue;
								}
								
								String time = tf.format(datetime);
								hour = (double) Integer.parseInt(time.substring(0, time.indexOf(":")))
										+ (double) Integer
												.parseInt(time.substring(time.indexOf(":") + 1, time.lastIndexOf(":"))) / 60
										+ (double) Integer.parseInt(time.substring(time.lastIndexOf(":") + 1)) / 3600;
		
			
								Double magnitude = Math
										.sqrt(Math
												.pow(Double.parseDouble(text.substring(text.indexOf(",") + 1,
														text.indexOf(",", text.indexOf(",") + 1))), 2)
												+ Math.pow(Double.parseDouble(text.substring(
														text.indexOf(",", text.indexOf(",") + 1) + 1, text.lastIndexOf(","))), 2)
												+ Math.pow(Double.parseDouble(text.substring(text.lastIndexOf(",") + 1)), 2));
								
								this.sumMagnitudeW += magnitude;
								text = buffered.readLine();
							
							} while (hour - avehour < CHUNK && text != null && countOccurrences(text, ',') < 4);
		
							day.add(new ArrayList<Double>() {
								{
									add((double) aveDate.getTime());
									add(Math.floor(singleton.sumMagnitudeW / numberW * 100) / 100);
								}
							});
		
						}
		
						buffered.close();
					}
								
					if(!day.isEmpty())
						ret.add(day);
					day = new ArrayList<ArrayList<Double>>();
				}
			}
		}

		return ret;

	}
	
	public static int countOccurrences(String haystack, char needle)
	{
	    int count = 0;
	    for (int i=0; i < haystack.length(); i++)
	    {
	        if (haystack.charAt(i) == needle)
	        {
	             count++;
	        }
	    }
	    return count;
	}
}

class Runnable1 implements Runnable{
    public void run(){
    	try {
			Preprocessor.singleton.data = Preprocessor.singleton.getData();
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}

class Runnable2 implements Runnable{
    public void run(){
    	try {
			Preprocessor.singleton.dataW = Preprocessor.singleton.getDataW();
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}

class Runnable3 implements Runnable{
    public void run(){
    	try {
			Preprocessor.singleton.battery = Preprocessor.singleton.getBattery();
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}

class Runnable4 implements Runnable{
	public void run(){
    	try {
			Preprocessor.singleton.batteryW = Preprocessor.singleton.getBatteryW();
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}

@XmlRootElement
class Activity {

    String name;
    String subname;

    public String getName() {
        return name;
    }

    @XmlElement
    public void setName(String name) {
        this.name = name;
    }

    public String getSubname() {
        return subname;
    }

    @XmlAttribute
    public void setId(String subname) {
        this.subname = subname;
    }

}
