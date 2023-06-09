import React from "react";
import { useLocation } from "react-router-dom";
import styled from "styled-components";
import "assets/font/font.css";

export default function Header () {
  const locationNow = useLocation();

  if (locationNow.pathname === "/") return null;
  return (
    <div>
      <MainBlock>
        <div className="header">
          <h3>건강해효</h3>
        </div>
      </MainBlock>
    </div>
  );
}

const MainBlock = styled.div`
  h3 {
    font-family: "Korail Round Gothic Bold";
    font-size: 3.5rem;
    margin: 0;
}

`;
