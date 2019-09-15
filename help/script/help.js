/**
 * 
 */
function init()
{
    chapter(0);
}

function chapter(chapterIndex)
{
    // 章、節、数式、図表を自動番号付け
    var path = new Array("chapter", "section", "subsection");
    var pathIndex = new Array();
    for (i = 0; i < path.length; i++)
    {
	pathIndex[i] = 0;
    }
    var indexContents = new Array();
    var eq = new Array("equation", "figure", "table");
    var eqIndex = new Array();
    for (i = 0; i < eq.length; i++)
    {
	eqIndex[i] = 0;
    }
    pathIndex[0] = chapterIndex;
    var eqCaption = new Array();
    var ps = document.getElementsByTagName("div");
    for (i = 0; i < ps.length; i++)
    {
	var name = ps[i].className;
	for (j = 0; j < path.length; j++)
	{
	    if (name == path[j])
	    {
		pathIndex[j]++;
		header = "";
		for (k = 0; k < j; k++)
		{
		    header += pathIndex[k] + ".";
		}
		switch(j)
		{
		case 0:
		    header += pathIndex[j] + " ";
		    break;
		case 1:
		    header += pathIndex[j] + " ";
		    // 数式番号のリセット
		    for (k = 0; k < eq.length; k++)
		    {
			eqIndex[k] = 0;
		    }
		    break;
		default:
		    header += pathIndex[j] + " ";
		}
		// 改行文字を削除
		var re = new RegExp("\n","g");
		var title = ps[i].innerHTML.replace(re,"");
		ps[i].innerHTML = header + title + "<a href=\"#top\" style=\"position:static;float:right;\">先頭に戻る</a>";
		for (k = (j+1) ; k < pathIndex.length; k++)
		{
		    pathIndex[k] = 0;
		}
		// タイトルを記憶
		if (1 == j)
		{
		    ps[i].id = title;
		    indexContents[pathIndex[1]-1] = header + "<a href=\"#" + title + "\">" + title + "</a>";
		}
		break;
	    }
	}
	// 数式、図表番号
	for (j = 0; j < eq.length; j++)
	{
	    if (name == eq[j])
	    {
		eqIndex[j]++;
		header = "";
		for (k = 0; k < pathIndex.length; k++)
		{
		    header += pathIndex[k] + ".";
		}
		switch(j)
		{
		case 0:
		    ps[i].innerHTML = "<center>" + ps[i].innerHTML + "<span class='number'>(" + header + eqIndex[j] + ")</span></center>";
		    break;
		case 1:
		    var caption = ps[i].getAttribute("caption");
		    eqCaption[caption] = "図 " + header + eqIndex[j] + "：" + caption;
		    ps[i].innerHTML = "<center>" + ps[i].innerHTML + "</center><center>" + eqCaption[caption] + "</center>";
		    break;
		case 2:
		    var caption = ps[i].getAttribute("caption");
		    eqCaption[caption] = "表 " + header + eqIndex[j] + "：" + caption;
		    ps[i].innerHTML = "<center>" + ps[i].innerHTML + "</center><center>" + eqCaption[caption] + "</center>";
		    break;
		}
		break;
	    }
	}
    }

    // ページ内目次
    var divs = document.getElementsByTagName("div");
    for (i = 0; i < divs.length; i++)
    {
	var div = divs[i];
	if ("index" == div.className)
	{
	    if (0 == indexContents.length)
	    {
		div.className = "";
	    }
	    else
	    {
		var ul = document.createElement("ul");
		ul.className = "index";
		ul.innerHTML += "<li><a href=\"#top\">先頭</a>";
		for (j = 0; j < indexContents.length; j++)
		{
		    ul.innerHTML += "<li>" + indexContents[j];
		}
		ul.innerHTML += "</ul>";
		div.appendChild(ul);
	    }
	    break;
	}
    }

    // 参照の解決
    var refs = document.getElementsByTagName("ref");
    for (i = 0; i < refs.length; i++)
    {
	var caption = refs[i].getAttribute("caption");
	refs[i].innerHTML = eqCaption[caption];
    }

    var htmls = document.getElementsByTagName("html");
    console.log(htmls[0].innerHTML);
}
