<!DOCTYPE html>
<html><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8>
	<title>Поиск в книгах</title>
	<meta http-equiv="X-UA-Compatible" content="IE=8;charset=utf-8">

    <style type="text/css">
        .tdhover:hover {
            background: #099ECA;
        }
        </style>
</head>
<body>
    <p>
        Поиск книг по словам из них. Только дословное совпадение, без учета регистра. Выводятся первые 100 результатов в порядке убывания количества совпадений. Только русский текст. По нажатию на результат, скачается соответствующий файл.
    </p>

	<p>
		<form class="form">
			<table>

				<tr>
					<td class = "search_block">
						<input type="text" name="search_field" class="search_field" placeholder="Введите слово...">
						<input type="button" name="search" value="Найти" onClick="Search(this.form)">
					</td>
				</tr>
			</table>
		</form>

        <p id="search_result">
        </p>

<script language="javascript" type="text/javascript">

function httpGet(theUrl)
{
    var xmlHttp = new XMLHttpRequest({mozSystem: true});
    xmlHttp.open( "GET", theUrl, false ); // false for synchronous request
    xmlHttp.send();
    return xmlHttp;
}

function Search(form)
{
	val = form.search_field.value;

    getInfoReq = "http://212.22.93.24:8282/Serv/SearchForWord?text=" + val;
	M = httpGet(getInfoReq)

	var resultsJSON = JSON.parse(M.responseText);
    resParent = document.getElementById('search_result');
    resParent.innerHTML = '';

    for (let j = 0; j < resultsJSON.length; j++)
	{
        tbl = document.createElement('table');
        const tr = tbl.insertRow();
        const td = tr.insertCell();

        td.style.border = '1px solid black';
        td.style.padding = '10px'
        td.style.overflowWrap = 'anywhere'
        td.className ='tdhover';
        td.id = resultsJSON[j].file;

        td.appendChild(document.createTextNode("Файл: " + resultsJSON[j].file));
		td.appendChild(document.createElement("br"));
        td.appendChild(document.createTextNode("Совпадений: " + resultsJSON[j].count));
        td.appendChild(document.createElement("br"));

        td.onclick = function()
		{
			OnResultClick(td.id);
		};

        resParent.appendChild(tbl);
        resParent.appendChild(document.createElement("br"));
	}

    if(resultsJSON.length == 0)
    {
        resParent.appendChild(document.createTextNode("Ничего не нашлось :( "));
    }
}

function openInNewTab(url)
{
    window.open(url, '_blank').focus();
}

function OnResultClick(file)
{
    openInNewTab("http://212.22.93.24:8282/Serv/GetFile?name=" + file);
}

</script>

</p></body></html>